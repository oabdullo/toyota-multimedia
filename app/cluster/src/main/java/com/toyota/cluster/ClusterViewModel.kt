package com.toyota.cluster

import android.car.VehiclePropertyIds
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.launcher.core.car.CarPropertyRepository
import com.toyota.launcher.feature.drivemode.GearLabel
import com.toyota.launcher.feature.media.MediaSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Aggregates every signal the cluster shows into one [StateFlow]. The flows
 * fan in from:
 *   - VHAL via [CarPropertyRepository]  (speed / gear / range / fuel / temp)
 *   - SensorManager rotation vector     (compass heading)
 *   - MediaSessionRepository            (now-playing strip mirror)
 *   - System clock (1 Hz tick)          (HH:MM display)
 *
 * No state lives in composables; everything is recomposed off this single
 * source of truth.
 */
@HiltViewModel
class ClusterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    carProps: CarPropertyRepository,
    mediaRepo: MediaSessionRepository,
) : ViewModel() {

    // --- VHAL signals -------------------------------------------------------

    // PERF_VEHICLE_SPEED is m/s, float. Convert to whole mph.
    private val speedMph: Flow<Int> =
        carProps.sharedPropertyFlow<Float>(VehiclePropertyIds.PERF_VEHICLE_SPEED)
            .map { (it * MPS_TO_MPH).toInt().coerceAtLeast(0) }
            .onStart { emit(0) }

    private val gear: Flow<GearLabel> =
        carProps.sharedPropertyFlow<Int>(VehiclePropertyIds.GEAR_SELECTION)
            .map { GearLabel.from(it) }
            .onStart { emit(GearLabel.P) }

    // Range is in meters per VHAL; convert to whole miles. The property may
    // not be implemented on the reference VHAL - keep `null` if absent.
    private val rangeMi: Flow<Int?> =
        carProps.sharedPropertyFlow<Float>(VehiclePropertyIds.RANGE_REMAINING)
            .map<Float, Int?> { (it * METERS_TO_MILES).toInt() }
            .onStart { emit(null) }

    // FUEL_LEVEL is in millilitres; we render as a coarse 0..100 fill so we
    // don't need FUEL_LEVEL_LOW thresholds.
    private val fuelLevelPct: Flow<Int?> =
        carProps.sharedPropertyFlow<Float>(VehiclePropertyIds.FUEL_LEVEL)
            .map<Float, Int?> { ml ->
                // Tank size varies by car. Use FUEL_VOLUME_DISPLAY_UNITS or
                // INFO_FUEL_CAPACITY when available; default to 60L (~ a
                // typical Toyota tank) so we always have a 0..100 number.
                val tankMl = 60_000f
                ((ml / tankMl) * 100f).toInt().coerceIn(0, 100)
            }
            .onStart { emit(null) }

    private val outsideTempF: Flow<Int?> =
        carProps.sharedPropertyFlow<Float>(VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE)
            .map<Float, Int?> { c -> (c * 9f / 5f + 32f).toInt() }
            .onStart { emit(null) }

    private val isReady: Flow<Boolean> =
        carProps.sharedPropertyFlow<Int>(VehiclePropertyIds.IGNITION_STATE)
            .map { it == IGNITION_STATE_ON || it == IGNITION_STATE_START }
            .onStart { emit(true) }

    // --- Sensors: compass heading ------------------------------------------

    private val headingDeg: Flow<Float?> = sensorHeadingFlow().onStart { emit(null) }

    // --- Clock --------------------------------------------------------------

    private val clock: Flow<String> = flow {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            emit(fmt.format(Date()))
            kotlinx.coroutines.delay(15_000L) // 15s is plenty for HH:MM
        }
    }

    // --- Media (mirrored from IVI) -----------------------------------------

    private val media: Flow<Pair<String, String>> = mediaRepo.observeTopSession()
        .map { c ->
            val snap = mediaRepo.snapshot(c)
            if (!snap.hasSession) FALLBACK_MEDIA
            else (snap.title to snap.subtitle)
        }
        .onStart { emit(FALLBACK_MEDIA) }

    // --- ADAS (mocked) ------------------------------------------------------
    //
    // Real signals (Android 14+): CRUISE_CONTROL_STATE,
    // ADAPTIVE_CRUISE_CONTROL_TARGET_SPEED, LANE_CENTERING_ASSIST_STATE.
    // On the AVD they're not implemented, so we hold them as MutableStateFlow
    // and expose hooks the demo can toggle. Wire to the real props by
    // swapping these two lines for sharedPropertyFlow() calls.
    private val accStateMock = MutableStateFlow(ClusterUiState.AccState.READY)
    private val laneCenteringMock = MutableStateFlow(false)

    // --- Fan-in -------------------------------------------------------------

    val state: StateFlow<ClusterUiState> = combine(
        combine(speedMph, gear, rangeMi, fuelLevelPct, outsideTempF) { s, g, r, f, t ->
            Quint(s, g, r, f, t)
        },
        combine(isReady, headingDeg, clock, media) { ready, hdg, clk, m ->
            Quad(ready, hdg, clk, m)
        },
        combine(accStateMock, laneCenteringMock) { a, l -> a to l },
    ) { vhal, sys, adas ->
        ClusterUiState(
            speedMph = vhal.a,
            gear = vhal.b,
            rangeMi = vhal.c,
            fuelLevelPct = vhal.d,
            outsideTempF = vhal.e,
            isReady = sys.a,
            headingDeg = sys.b,
            clock = sys.c,
            mediaTitle = sys.d.first,
            mediaSubtitle = sys.d.second,
            acc = adas.first,
            laneCenteringEngaged = adas.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClusterUiState())

    // --- Helpers -----------------------------------------------------------

    /**
     * Bridges SensorManager's rotation vector to a heading-in-degrees flow.
     * We pick TYPE_ROTATION_VECTOR because it fuses accelerometer +
     * magnetometer + gyro and gives a stable heading - the same approach
     * Google's reference cluster compass uses. Returns `Flow<Float?>` so
     * the upstream pipeline can express "no fix yet" without a sentinel
     * value.
     */
    private fun sensorHeadingFlow(): Flow<Float?> = callbackFlow {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rot = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sm == null || rot == null) {
            close(); return@callbackFlow
        }
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                trySend((deg + 360f) % 360f)
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }
        sm.registerListener(listener, rot, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sm.unregisterListener(listener) }
    }

    private data class Quint<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private companion object {
        const val MPS_TO_MPH = 2.23694f
        const val METERS_TO_MILES = 0.000621371f
        // IgnitionState constants - inlined to avoid pulling android.car
        // into compilation when the stub jar omits the enum.
        const val IGNITION_STATE_ON = 4
        const val IGNITION_STATE_START = 5
        val FALLBACK_MEDIA = "KNTU 88.1" to ""
    }
}

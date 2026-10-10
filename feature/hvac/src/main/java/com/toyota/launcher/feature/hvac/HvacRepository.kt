package com.toyota.launcher.feature.hvac

import android.car.VehicleAreaSeat
import android.car.VehiclePropertyIds
import com.toyota.launcher.core.car.CarPropertyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * HVAC adapter on top of [CarPropertyRepository]. Owns the mapping from raw
 * VHAL properties + zones to a single [HvacState] the UI can render.
 *
 * Property reference (selected):
 *   HVAC_TEMPERATURE_SET     - per-seat float, Celsius
 *   HVAC_TEMPERATURE_DISPLAY_UNITS - global int, 0x30=Celsius 0x31=Fahrenheit
 *   HVAC_FAN_SPEED           - global int, range from VHAL config
 *   HVAC_AUTO_ON             - global bool
 *   HVAC_AC_ON               - global bool
 *   HVAC_DEFROSTER (areaId=window) - per-window bool
 *   HVAC_SEAT_TEMPERATURE    - per-seat int (-3..3 cool/heat)
 *   HVAC_DUAL_ON             - global bool (driver/passenger sync)
 */
@Singleton
class HvacRepository @Inject constructor(
    private val carProps: CarPropertyRepository,
) {

    private val driverArea = VehicleAreaSeat.SEAT_ROW_1_LEFT
    private val passengerArea = VehicleAreaSeat.SEAT_ROW_1_RIGHT

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<HvacState> {
        val tempDriver = carProps.sharedPropertyFlow<Float>(
            VehiclePropertyIds.HVAC_TEMPERATURE_SET, driverArea
        ).onStart { emit(20f) }

        val tempPassenger = carProps.sharedPropertyFlow<Float>(
            VehiclePropertyIds.HVAC_TEMPERATURE_SET, passengerArea
        ).onStart { emit(20f) }

        val seatHeatDriver = carProps.sharedPropertyFlow<Int>(
            VehiclePropertyIds.HVAC_SEAT_TEMPERATURE, driverArea
        ).onStart { emit(0) }

        val seatHeatPassenger = carProps.sharedPropertyFlow<Int>(
            VehiclePropertyIds.HVAC_SEAT_TEMPERATURE, passengerArea
        ).onStart { emit(0) }

        val fan = carProps.sharedPropertyFlow<Int>(VehiclePropertyIds.HVAC_FAN_SPEED)
            .onStart { emit(0) }

        val auto = carProps.sharedPropertyFlow<Boolean>(VehiclePropertyIds.HVAC_AUTO_ON)
            .onStart { emit(false) }

        val ac = carProps.sharedPropertyFlow<Boolean>(VehiclePropertyIds.HVAC_AC_ON)
            .onStart { emit(false) }

        val defrosterFront = carProps.sharedPropertyFlow<Boolean>(
            VehiclePropertyIds.HVAC_DEFROSTER, WINDOW_FRONT_WINDSHIELD
        ).onStart { emit(false) }

        val defrosterRear = carProps.sharedPropertyFlow<Boolean>(
            VehiclePropertyIds.HVAC_DEFROSTER, WINDOW_REAR_WINDSHIELD
        ).onStart { emit(false) }

        // HVAC_DUAL_ON is per-seat (NOT global) on most VHAL implementations,
        // typically owned by the driver-side seat area. Reading it with
        // areaId=0 throws IllegalArgumentException.
        val sync = carProps.sharedPropertyFlow<Boolean>(
            VehiclePropertyIds.HVAC_DUAL_ON, driverArea
        ).onStart { emit(false) }

        val units = carProps.sharedPropertyFlow<Int>(
            VehiclePropertyIds.HVAC_TEMPERATURE_DISPLAY_UNITS
        ).onStart { emit(UNIT_FAHRENHEIT) }

        // Combine into a single state. We split into two combines because
        // Flow.combine is variadic-limited; chaining is fine.
        val seatAndFan = combine(seatHeatDriver, seatHeatPassenger, fan, auto, ac) {
            sd, sp, f, a, ac -> Quint(sd, sp, f, a, ac)
        }
        val defAndSync = combine(defrosterFront, defrosterRear, sync, units) {
            df, dr, sy, u -> Quad(df, dr, sy, u)
        }

        return combine(tempDriver, tempPassenger, seatAndFan, defAndSync) {
            tD, tP, sf, ds ->
            HvacState(
                driverTempC = tD,
                passengerTempC = tP,
                driverSeatHeatLevel = sf.a,
                passengerSeatHeatLevel = sf.b,
                fanSpeed = sf.c,
                fanSpeedMax = FAN_SPEED_MAX,
                autoOn = sf.d,
                acOn = sf.e,
                defrosterFront = ds.a,
                defrosterRear = ds.b,
                syncOn = ds.c,
                displayUnitFahrenheit = ds.d == UNIT_FAHRENHEIT,
            )
        }.map { it }
    }

    fun adjustDriverTemp(deltaC: Float) {
        val current = carProps.readOnce<Float>(
            VehiclePropertyIds.HVAC_TEMPERATURE_SET, driverArea
        ) ?: 20f
        val next = (current + deltaC).coerceIn(MIN_TEMP_C, MAX_TEMP_C)
        carProps.write(VehiclePropertyIds.HVAC_TEMPERATURE_SET, driverArea, next)
    }

    fun adjustPassengerTemp(deltaC: Float) {
        val current = carProps.readOnce<Float>(
            VehiclePropertyIds.HVAC_TEMPERATURE_SET, passengerArea
        ) ?: 20f
        val next = (current + deltaC).coerceIn(MIN_TEMP_C, MAX_TEMP_C)
        carProps.write(VehiclePropertyIds.HVAC_TEMPERATURE_SET, passengerArea, next)
    }

    fun cycleFan() {
        val current = carProps.readOnce<Int>(VehiclePropertyIds.HVAC_FAN_SPEED) ?: 0
        val next = if (current >= FAN_SPEED_MAX) 0 else current + 1
        carProps.write(VehiclePropertyIds.HVAC_FAN_SPEED, 0, next)
    }

    fun toggleAuto() {
        val current = carProps.readOnce<Boolean>(VehiclePropertyIds.HVAC_AUTO_ON) ?: false
        carProps.write(VehiclePropertyIds.HVAC_AUTO_ON, 0, !current)
    }

    fun toggleSync() {
        val current = carProps.readOnce<Boolean>(
            VehiclePropertyIds.HVAC_DUAL_ON, driverArea
        ) ?: false
        carProps.write(VehiclePropertyIds.HVAC_DUAL_ON, driverArea, !current)
    }

    fun toggleFrontDefrost() {
        val current = carProps.readOnce<Boolean>(
            VehiclePropertyIds.HVAC_DEFROSTER, WINDOW_FRONT_WINDSHIELD
        ) ?: false
        carProps.write(VehiclePropertyIds.HVAC_DEFROSTER, WINDOW_FRONT_WINDSHIELD, !current)
    }

    fun toggleRearDefrost() {
        val current = carProps.readOnce<Boolean>(
            VehiclePropertyIds.HVAC_DEFROSTER, WINDOW_REAR_WINDSHIELD
        ) ?: false
        carProps.write(VehiclePropertyIds.HVAC_DEFROSTER, WINDOW_REAR_WINDSHIELD, !current)
    }

    fun cycleDriverSeatHeat() = cycleSeatHeat(driverArea)
    fun cyclePassengerSeatHeat() = cycleSeatHeat(passengerArea)

    private fun cycleSeatHeat(area: Int) {
        val current = carProps.readOnce<Int>(VehiclePropertyIds.HVAC_SEAT_TEMPERATURE, area) ?: 0
        // -3..3 cool/heat range; we cycle 0 -> 1 -> 2 -> 3 -> 0 for a heat-only chip.
        val next = if (current >= SEAT_HEAT_MAX) 0 else (current + 1).coerceAtLeast(0)
        carProps.write(VehiclePropertyIds.HVAC_SEAT_TEMPERATURE, area, next)
    }

    private companion object {
        const val MIN_TEMP_C = 16f
        const val MAX_TEMP_C = 32f
        const val FAN_SPEED_MAX = 7
        const val SEAT_HEAT_MAX = 3
        const val UNIT_FAHRENHEIT = 0x31

        // android.car.VehicleAreaWindow constants - inlined because the
        // class is not exposed in every android.car stub jar variant.
        const val WINDOW_FRONT_WINDSHIELD = 0x00000001
        const val WINDOW_REAR_WINDSHIELD = 0x00000400
    }
}

/** Tiny tuple holders to keep combine() arity manageable. */
private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Quint<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

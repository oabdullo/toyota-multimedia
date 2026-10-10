package com.toyota.launcher.core.car

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob

/**
 * Bridges [CarPropertyManager]'s callback-style API into a coroutine [Flow].
 *
 * Why callbackFlow vs. polling? Vehicle properties (HVAC temp, gear, etc.) are
 * change-sensor-rate by default - the VHAL pushes events when the value
 * changes. Polling wastes CPU and creates inconsistent UI. With
 * [CarPropertyManager.SENSOR_RATE_ONCHANGE] the flow only emits on real
 * delta and Compose only recomposes when StateFlow downstream actually
 * changes value.
 *
 * The [connection] is awaited via flatMapLatest so the flow auto-resubscribes
 * if the car service reconnects.
 */
@Singleton
class CarPropertyRepository @Inject constructor(
    private val carConnection: CarConnectionManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Properties we've already discovered we can't write/read (typically
     * because the app isn't in /system/priv-app and the privileged car
     * permission is denied). Each property is logged with the fix once;
     * subsequent attempts are silently swallowed so logcat stays usable.
     */
    private val deniedProps = ConcurrentHashMap.newKeySet<Int>()

    /**
     * Emits whenever [propId] changes for [areaId]. T is the property's
     * intrinsic type (Int, Float, Boolean, ...). Caller is responsible for
     * matching T to the property definition; mismatch throws ClassCastException.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : Any> propertyFlow(propId: Int, areaId: Int = 0): Flow<T> =
        carConnection.connection.flatMapLatest { state ->
            if (state !is CarConnectionManager.State.Connected) {
                flow {} // wait for connection
            } else {
                val cpm = state.car.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
                if (cpm == null) {
                    Log.w(TAG, "PROPERTY_SERVICE unavailable")
                    flow {}
                } else {
                    propertyCallbackFlow<T>(cpm, propId, areaId)
                }
            }
        }.flowOn(Dispatchers.Default)

    /**
     * Hot, replay-1 version of [propertyFlow]. Use for properties read by
     * multiple composables (HVAC temp, gear) - avoids duplicate VHAL
     * subscriptions.
     */
    fun <T : Any> sharedPropertyFlow(propId: Int, areaId: Int = 0): Flow<T> =
        propertyFlow<T>(propId, areaId)
            .shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    /**
     * One-shot read. Returns null if the property isn't available or the
     * caller lacks permission - never throws to callers.
     */
    fun <T : Any> readOnce(propId: Int, areaId: Int = 0): T? {
        val state = carConnection.connection.value
        if (state !is CarConnectionManager.State.Connected) return null
        if (propId in deniedProps) return null
        val cpm = state.car.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            ?: return null
        return try {
            @Suppress("UNCHECKED_CAST")
            cpm.getProperty<Any>(propId, areaId).value as? T
        } catch (sec: SecurityException) {
            markDenied(propId, "read", sec.message)
            null
        } catch (iae: IllegalArgumentException) {
            // VHAL reports the property or area is not implemented. Same
            // user-visible outcome as a permission denial - record it once.
            markDenied(propId, "read", iae.message)
            null
        } catch (t: Throwable) {
            Log.w(TAG, "readOnce($propId,$areaId) failed", t)
            null
        }
    }

    /**
     * Write with best-effort error swallowing. Returns true on success.
     * In practice on AAOS the call is async; the value is reflected back
     * via the change callback when the VHAL accepts it.
     *
     * The first SecurityException for any property is logged once at INFO
     * with the actionable fix; subsequent writes to the same property are
     * silent so a button mash doesn't spam logcat.
     */
    fun <T : Any> write(propId: Int, areaId: Int, value: T): Boolean {
        val state = carConnection.connection.value
        if (state !is CarConnectionManager.State.Connected) return false
        if (propId in deniedProps) return false
        val cpm = state.car.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            ?: return false
        return try {
            @Suppress("UNCHECKED_CAST")
            cpm.setProperty(value::class.java as Class<T>, propId, areaId, value)
            true
        } catch (sec: SecurityException) {
            markDenied(propId, "write", sec.message)
            false
        } catch (iae: IllegalArgumentException) {
            markDenied(propId, "write", iae.message)
            false
        } catch (t: Throwable) {
            Log.w(TAG, "write($propId,$areaId,$value) failed", t)
            false
        }
    }

    /**
     * Has [readOnce] or [write] for [propId] been rejected by the platform
     * for lack of a privileged grant? UI layers can use this to dim
     * controls and show a "Privileged install required" hint instead of
     * looking unresponsive.
     */
    fun isDenied(propId: Int): Boolean = propId in deniedProps

    /**
     * First time a property fails with a permission denial or
     * "not implemented" error, log one clean line at INFO and remember the
     * propId so we don't retry. The propId is rendered in hex so it can be
     * grepped directly against `VehiclePropertyIds.*` constants.
     */
    private fun markDenied(propId: Int, op: String, reason: String?) {
        if (deniedProps.add(propId)) {
            val hint = when {
                reason?.contains("permission", ignoreCase = true) == true ->
                    "Install to /system/priv-app via tools/scripts/install-priv.sh."
                reason?.contains("not supported", ignoreCase = true) == true ||
                    reason?.contains("not implemented", ignoreCase = true) == true ->
                    "VHAL does not implement this property on this image."
                else -> "See VehiclePropertyIds for valid usage."
            }
            Log.i(
                TAG,
                "Property $op unavailable for propId=0x${propId.toString(16)} " +
                    "($reason). $hint",
            )
        }
    }

    private fun <T : Any> propertyCallbackFlow(
        cpm: CarPropertyManager,
        propId: Int,
        areaId: Int,
    ): Flow<T> = callbackFlow {
        if (propId in deniedProps) {
            close(); return@callbackFlow
        }

        // Seed with current value so subscribers don't wait for next change.
        // Catch SecurityException and IllegalArgumentException explicitly so
        // missing permissions / unsupported properties don't produce a
        // stack-trace storm at startup; runCatching would swallow them but
        // we want to record them as denied so future calls short-circuit.
        try {
            @Suppress("UNCHECKED_CAST")
            (cpm.getProperty<Any>(propId, areaId).value as? T)?.let { trySend(it) }
        } catch (sec: SecurityException) {
            markDenied(propId, "read", sec.message)
            close(); return@callbackFlow
        } catch (iae: IllegalArgumentException) {
            markDenied(propId, "read", iae.message)
            close(); return@callbackFlow
        } catch (_: Throwable) { /* property may simply not be implemented */ }

        val cb = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.areaId != areaId) return
                @Suppress("UNCHECKED_CAST")
                (value.value as? T)?.let { trySend(it) }
            }

            override fun onErrorEvent(propId: Int, areaId: Int) {
                Log.w(TAG, "Property error propId=$propId areaId=$areaId")
            }
        }

        val registered = try {
            cpm.registerCallback(cb, propId, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        } catch (sec: SecurityException) {
            markDenied(propId, "subscribe", sec.message)
            false
        } catch (iae: IllegalArgumentException) {
            markDenied(propId, "subscribe", iae.message)
            false
        } catch (t: Throwable) {
            Log.w(TAG, "registerCallback($propId) failed", t)
            false
        }

        if (!registered) {
            close()
            return@callbackFlow
        }

        awaitClose { runCatching { cpm.unregisterCallback(cb, propId) } }
    }

    private companion object { const val TAG = "CarPropertyRepository" }
}

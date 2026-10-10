package com.toyota.launcher.core.car

import android.car.Car
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns the lifetime of the [Car] connection to the car service. Process-wide
 * singleton so every feature module shares one connection (the car service
 * supports multiple manager handles per process but we only want one
 * subscription/teardown lifecycle to reason about).
 *
 * The Toyota launcher is a HOME activity so it lives for the whole user
 * session - we connect on first request and never explicitly disconnect.
 * If you ever build a non-launcher app reusing this, scope it to the
 * Activity lifecycle and call [disconnect] from onDestroy.
 *
 * Connection is async: `Car.createCar` with `CAR_WAIT_TIMEOUT_WAIT_FOREVER`
 * retries forever (car service may not be ready immediately at boot).
 * Consumers observe [connection] and call [getService] only after state
 * becomes [Connected].
 */
@Singleton
class CarConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    sealed interface State {
        data object Disconnected : State
        data object Connecting : State
        data class Connected(val car: Car) : State
        data class Suspended(val cause: Int) : State
    }

    private val _connection = MutableStateFlow<State>(State.Disconnected)
    val connection: StateFlow<State> = _connection.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var car: Car? = null

    fun connect() {
        if (_connection.value !is State.Disconnected) return
        _connection.value = State.Connecting
        Log.i(TAG, "Connecting to car service...")
        car = Car.createCar(
            context,
            handler,
            Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER,
        ) { c, ready ->
            if (ready) {
                Log.i(TAG, "Car service ready")
                _connection.value = State.Connected(c)
            } else {
                Log.w(TAG, "Car service connection lost")
                _connection.value = State.Suspended(0)
            }
        }
    }

    fun disconnect() {
        car?.disconnect()
        car = null
        _connection.value = State.Disconnected
    }

    /** Convenience: get a manager service or null if not yet connected. */
    inline fun <reified T : Any> getService(serviceName: String): T? {
        val state = connection.value
        return if (state is State.Connected) {
            @Suppress("UNCHECKED_CAST")
            state.car.getCarManager(serviceName) as? T
        } else null
    }

    private companion object {
        const val TAG = "CarConnectionManager"
    }
}

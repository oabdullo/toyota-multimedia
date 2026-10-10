package com.toyota.launcher.feature.uxrestrictions

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.util.Log
import com.toyota.launcher.core.car.CarConnectionManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Wraps [CarUxRestrictionsManager] so the UI can dim/disable interactions
 * while the vehicle is in motion. NHTSA / Google guidelines require any
 * driver-facing app on AAOS to honor this; consumer apps that ignore it
 * can be rejected from Play.
 */
@Singleton
class UxRestrictionsRepository @Inject constructor(
    private val carConnection: CarConnectionManager,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<UxRestrictionsSnapshot> =
        carConnection.connection.flatMapLatest { state ->
            if (state !is CarConnectionManager.State.Connected) flow {
                emit(UxRestrictionsSnapshot.Unrestricted)
            } else {
                val mgr = state.car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE)
                    as? CarUxRestrictionsManager
                if (mgr == null) flow { emit(UxRestrictionsSnapshot.Unrestricted) }
                else uxFlow(mgr)
            }
        }

    private fun uxFlow(mgr: CarUxRestrictionsManager): Flow<UxRestrictionsSnapshot> =
        callbackFlow {
            val emit: (CarUxRestrictions?) -> Unit = { r ->
                trySend(snap(r))
            }
            // Seed.
            emit(runCatching { mgr.currentCarUxRestrictions }.getOrNull())

            val listener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { r ->
                emit(r)
            }
            try {
                mgr.registerListener(listener)
            } catch (t: Throwable) {
                Log.w(TAG, "registerListener failed", t)
                trySend(UxRestrictionsSnapshot.Unrestricted)
            }
            awaitClose { runCatching { mgr.unregisterListener() } }
        }

    private fun snap(r: CarUxRestrictions?): UxRestrictionsSnapshot {
        if (r == null) return UxRestrictionsSnapshot.Unrestricted
        val isParked = !r.isRequiresDistractionOptimization
        val flags = r.activeRestrictions
        return UxRestrictionsSnapshot(
            isParked = isParked,
            noKeyboard = flags and CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD != 0,
            noVideo = flags and CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO != 0,
            limitContent = flags and CarUxRestrictions.UX_RESTRICTIONS_LIMIT_CONTENT != 0,
            noSetup = flags and CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP != 0,
        )
    }

    private companion object { const val TAG = "UxRestrictionsRepo" }
}

data class UxRestrictionsSnapshot(
    val isParked: Boolean = true,
    val noKeyboard: Boolean = false,
    val noVideo: Boolean = false,
    val limitContent: Boolean = false,
    val noSetup: Boolean = false,
) {
    val isDriving: Boolean get() = !isParked

    companion object {
        val Unrestricted = UxRestrictionsSnapshot()
    }
}

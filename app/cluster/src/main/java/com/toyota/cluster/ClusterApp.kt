package com.toyota.cluster

import android.app.Application
import com.toyota.launcher.core.car.CarConnectionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Cluster app process entry point. Runs independently from the launcher
 * (different applicationId, different PID) - if the IVI crashes, the
 * cluster keeps drawing speed/gear. That isolation is the whole point of
 * AAOS treating the cluster as its own display + Activity stack.
 */
@HiltAndroidApp
class ClusterApp : Application() {

    @Inject lateinit var carConnection: CarConnectionManager

    override fun onCreate() {
        super.onCreate()
        // Eagerly subscribe to the Car service; the cluster is on whenever
        // the car is on, so the connection lifetime == process lifetime.
        carConnection.connect()
    }
}

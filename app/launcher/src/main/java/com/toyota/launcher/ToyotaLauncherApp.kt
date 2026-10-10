package com.toyota.launcher

import android.app.Application
import com.toyota.launcher.core.car.CarConnectionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ToyotaLauncherApp : Application() {

    @Inject lateinit var carConnection: CarConnectionManager

    override fun onCreate() {
        super.onCreate()
        // Eagerly connect to the car service. The launcher is a HOME activity
        // so this is effectively the user-session lifetime.
        carConnection.connect()
    }
}

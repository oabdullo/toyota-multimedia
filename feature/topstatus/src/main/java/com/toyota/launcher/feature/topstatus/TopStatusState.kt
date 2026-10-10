package com.toyota.launcher.feature.topstatus

data class TopStatusState(
    val timeText: String = "",
    val signalBars: Int = 0, // 0..4
    val carrier: String = "",
    val bluetoothOn: Boolean = false,
    val microphoneActive: Boolean = false,
    val locationEnabled: Boolean = false,
    val unreadNotifications: Int = 0,
)

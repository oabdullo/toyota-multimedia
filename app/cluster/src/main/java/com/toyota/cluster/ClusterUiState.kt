package com.toyota.cluster

import com.toyota.launcher.feature.drivemode.GearLabel

/**
 * Single immutable snapshot the cluster UI renders. Everything that isn't
 * available from the VHAL on the reference AVD is `null` here so widgets
 * can decide whether to show the value or a dim placeholder.
 */
data class ClusterUiState(
    // Speed / powertrain
    val speedMph: Int = 0,
    val gear: GearLabel = GearLabel.P,
    // Energy
    val rangeMi: Int? = null,
    val fuelLevelPct: Int? = null, // 0..100
    // Environment
    val outsideTempF: Int? = null,
    val clock: String = "--:--",
    // Drivetrain readiness (READY light on a hybrid / EV)
    val isReady: Boolean = true,
    // Compass heading, degrees clockwise from north (0..359). Null = no fix.
    val headingDeg: Float? = null,
    // Media (mirrored from whatever's playing on the IVI)
    val mediaTitle: String = "KNTU 88.1",
    val mediaSubtitle: String = "",
    // ADAS (mocked for AVD; real signals listed in CarPropertyIds doc)
    val acc: AccState = AccState.OFF,
    val laneCenteringEngaged: Boolean = false,
    // Mock trip distance, miles (no portable VHAL property).
    val tripMi: Int = 447,
    // Mock instantaneous mpg.
    val mpg: Float = 0.0f,
) {
    enum class AccState { OFF, READY, ENGAGED }
}

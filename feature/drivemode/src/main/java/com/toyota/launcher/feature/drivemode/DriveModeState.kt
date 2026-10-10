package com.toyota.launcher.feature.drivemode

/**
 * Drive mode is an OEM concept (Eco / Normal / Sport / Custom) that doesn't
 * have a portable VHAL property in stock AAOS yet. We expose it as an in-app
 * enum that the user toggles. The current gear DOES come from the VHAL.
 */
enum class DriveMode(val label: String) {
    ECO("Eco"),
    NORMAL("Normal"),
    SPORT("Sport"),
    CUSTOM("Custom"),
}

enum class GearLabel(val label: String) {
    P("P"), R("R"), N("N"), D("D"), UNKNOWN("");

    companion object {
        // VehiclePropertyIds.GEAR_SELECTION uses VehicleGear bit constants.
        // These are stable across AAOS versions; kept inline to avoid forcing
        // a dep on android.car for a mapping.
        private const val GEAR_PARK = 0x0004
        private const val GEAR_REVERSE = 0x0002
        private const val GEAR_NEUTRAL = 0x0001
        private const val GEAR_DRIVE = 0x0008

        fun from(raw: Int): GearLabel = when {
            raw and GEAR_PARK != 0 -> P
            raw and GEAR_REVERSE != 0 -> R
            raw and GEAR_NEUTRAL != 0 -> N
            raw and GEAR_DRIVE != 0 || raw >= 0x0010 -> D // any forward gear bit
            else -> UNKNOWN
        }
    }
}

data class DriveModeUiState(
    val mode: DriveMode = DriveMode.NORMAL,
    val gear: GearLabel = GearLabel.P,
)

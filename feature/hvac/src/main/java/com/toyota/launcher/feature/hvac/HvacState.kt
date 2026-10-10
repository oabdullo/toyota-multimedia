package com.toyota.launcher.feature.hvac

/**
 * Snapshot of the HVAC strip from the user's POV. All temps in Celsius
 * because that's what HVAC_TEMPERATURE_SET stores; the view layer
 * formats to F using HVAC_TEMPERATURE_DISPLAY_UNITS (or device locale).
 */
data class HvacState(
    val driverTempC: Float = 20f,
    val passengerTempC: Float = 20f,
    val driverSeatHeatLevel: Int = 0,
    val passengerSeatHeatLevel: Int = 0,
    val fanSpeed: Int = 0,
    val fanSpeedMax: Int = 7,
    val autoOn: Boolean = false,
    val acOn: Boolean = false,
    val defrosterFront: Boolean = false,
    val defrosterRear: Boolean = false,
    val syncOn: Boolean = false,
    val displayUnitFahrenheit: Boolean = true,
)

package com.toyota.launcher.core.design

import androidx.compose.ui.graphics.Color

/**
 * Toyota Multimedia palette, reverse-engineered from the reference screenshot.
 *
 * The dashboard chrome is near-black; the three primary cards each carry their
 * own brand color (warm brown for drive mode, blue for weather, green for FM).
 * Climate strip uses the same dark chrome with high-contrast white text.
 */
object ToyotaColors {
    // Chrome
    val Background = Color(0xFF0E0E10)
    val Surface = Color(0xFF1A1A1D)
    val SurfaceVariant = Color(0xFF26262B)
    val OutlineSubtle = Color(0xFF2F2F35)
    val OnSurface = Color(0xFFF2F2F2)
    val OnSurfaceMuted = Color(0xFFB7B7BC)

    // App rail (the lighter beige strip on the left)
    val AppRailBackground = Color(0xFFEDE9E2)
    val AppRailIconActive = Color(0xFFFFFFFF)
    val AppRailIconActiveBg = Color(0xFF1F5BFF)
    val AppRailIconInactive = Color(0xFF1A1A1D)

    // Drive mode card (warm brown / mocha)
    val DriveModeBg = Color(0xFF6E5447)
    val DriveModeChip = Color(0xFF5C4538)
    val DriveModeOnBg = Color(0xFFFFFFFF)

    // Weather card (deep sky blue)
    val WeatherBg = Color(0xFF2A6AD8)
    val WeatherSun = Color(0xFFFFC32E)
    val WeatherOnBg = Color(0xFFFFFFFF)

    // Media card (Indie 88.1 green)
    val MediaBg = Color(0xFF2E8F4F)
    val MediaOnBg = Color(0xFFFFFFFF)
    val MediaArtBg = Color(0xFFFFFFFF)

    // HVAC strip
    val HvacBg = Color(0xFF000000)
    val HvacOn = Color(0xFFFFFFFF)
    val HvacAccentRed = Color(0xFFE0533D)
    val HvacAccentBlue = Color(0xFF3D8BE0)
    val HvacSyncOn = Color(0xFF1F5BFF)
}

package com.toyota.launcher.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val ToyotaDarkColors = darkColorScheme(
    background = ToyotaColors.Background,
    surface = ToyotaColors.Surface,
    surfaceVariant = ToyotaColors.SurfaceVariant,
    onBackground = ToyotaColors.OnSurface,
    onSurface = ToyotaColors.OnSurface,
    onSurfaceVariant = ToyotaColors.OnSurfaceMuted,
    primary = ToyotaColors.AppRailIconActiveBg,
    onPrimary = ToyotaColors.OnSurface,
    secondary = ToyotaColors.WeatherBg,
)

@Composable
fun ToyotaLauncherTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = ToyotaDarkColors,
            typography = ToyotaTypography,
            content = content,
        )
    }
}

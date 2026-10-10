package com.toyota.launcher.core.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val cardCorner: Dp = 28.dp,
    val cardGap: Dp = 12.dp,
    val appRailWidth: Dp = 64.dp,
    val topBarHeight: Dp = 56.dp,
    val hvacStripHeight: Dp = 64.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

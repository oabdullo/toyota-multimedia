package com.toyota.launcher.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

/**
 * The reusable rounded "tile" used for Drive Mode, Weather, and Now Playing.
 * Matches the ~28dp corner radius and full-bleed background fill from the
 * reference screenshot.
 */
@Composable
fun BigCard(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = LocalSpacing.current.lg,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LocalSpacing.current.cardCorner))
            .background(backgroundColor)
            .padding(contentPadding),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

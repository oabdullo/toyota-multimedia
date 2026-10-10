package com.toyota.cluster.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toyota.cluster.ClusterUiState
import com.toyota.cluster.ui.ClusterColors

/**
 * Center pane: big speedometer, gear pill, outside temp + clock, and a
 * stylised "map" backdrop with a navigation arrow.
 *
 * The map is intentionally NOT a live MapView - that would require Maps SDK
 * + GMS, and burns GPU on a permanently-on display. We render a few
 * suggestive lines via Canvas to evoke the look without taking the cost.
 */
@Composable
fun SpeedoCenter(state: ClusterUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1424), Color(0xFF06101F)),
                )
            ),
    ) {
        MapBackdrop(modifier = Modifier.fillMaxSize())

        // Top row: outside temp + gear + clock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.outsideTempF?.let { "$it" + "°F" } ?: "--°F",
                color = ClusterColors.OnBg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            GearPill(state.gear.label.ifBlank { "—" })
            Text(
                text = state.clock,
                color = ClusterColors.OnBg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Center: speedometer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${state.speedMph}",
                color = ClusterColors.OnBg,
                fontSize = 96.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "MPH",
                color = ClusterColors.Muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 0.dp),
            )
        }

        // Bottom: nav arrow + destination chip
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Navigation,
                contentDescription = null,
                tint = ClusterColors.Accent,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun GearPill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1F5BFF))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

/**
 * Stylised, static "map" backdrop drawn as a handful of low-contrast curves
 * so the speedo stays readable. Replaced with a real map (Maps SDK,
 * NavRenderingService callback surface, etc.) for a production cluster.
 */
@Composable
private fun MapBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val road = ClusterColors.PanelStroke.copy(alpha = 0.55f)
        // A few diagonal "roads" suggesting a map plate
        drawLine(
            color = road,
            start = Offset(0f, h * 0.62f),
            end = Offset(w, h * 0.42f),
            strokeWidth = 2.dp.toPx(),
        )
        drawLine(
            color = road,
            start = Offset(w * 0.25f, 0f),
            end = Offset(w * 0.45f, h),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = road,
            start = Offset(w * 0.7f, 0f),
            end = Offset(w * 0.5f, h),
            strokeWidth = 1.dp.toPx(),
        )
        // Highlighted "current route"
        drawLine(
            color = ClusterColors.Accent.copy(alpha = 0.45f),
            start = Offset(w * 0.5f, h * 0.95f),
            end = Offset(w * 0.5f, h * 0.55f),
            strokeWidth = 3.dp.toPx(),
        )
        // Glow dot at current position
        drawCircle(
            color = ClusterColors.Accent.copy(alpha = 0.85f),
            radius = 6.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.95f),
        )
    }
}

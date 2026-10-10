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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toyota.cluster.ClusterUiState
import com.toyota.cluster.ui.ClusterColors

/**
 * Left card: compass + radio station + fuel/temp gauge + READY badge.
 *
 * Compass needle reflects [ClusterUiState.headingDeg] (rotation-vector
 * sensor). Cardinal letters (N/S/E/W) are static; the inner circle stays
 * upright while the outer ring rotates - common AAOS pattern.
 */
@Composable
fun CompassCard(state: ClusterUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ClusterColors.PanelBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            CompassRose(headingDeg = state.headingDeg)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A2438))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.mediaTitle,
                color = ClusterColors.OnBg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReadyBadge(isReady = state.isReady)
            Text(
                text = state.rangeMi?.let { "$it mi" } ?: "--- mi",
                color = ClusterColors.OnBg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        FuelBar(
            fuelPct = state.fuelLevelPct,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun CompassRose(headingDeg: Float?) {
    val heading = headingDeg ?: 0f
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            // Outer ring
            drawCircle(
                color = ClusterColors.PanelStroke,
                radius = r,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
            // Rotating tick ring
            rotate(degrees = -heading, pivot = center) {
                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angle = (i * (360f / tickCount)) * (Math.PI.toFloat() / 180f)
                    val isMajor = i % 6 == 0
                    val inner = r - if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                    val outer = r - 2.dp.toPx()
                    val sx = center.x + inner * kotlin.math.sin(angle)
                    val sy = center.y - inner * kotlin.math.cos(angle)
                    val ex = center.x + outer * kotlin.math.sin(angle)
                    val ey = center.y - outer * kotlin.math.cos(angle)
                    drawLine(
                        color = if (isMajor) ClusterColors.OnBg else ClusterColors.Muted,
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                    )
                }
            }
            // Stationary inner indicator
            drawCircle(
                color = ClusterColors.Accent,
                radius = 8.dp.toPx(),
                center = center,
            )
        }
        // Cardinal letters (static)
        CardinalLabel("N", Alignment.TopCenter)
        CardinalLabel("S", Alignment.BottomCenter)
        CardinalLabel("E", Alignment.CenterEnd, accent = true)
        CardinalLabel("W", Alignment.CenterStart)
    }
}

@Composable
private fun CardinalLabel(letter: String, alignment: Alignment, accent: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = alignment) {
        Text(
            text = letter,
            color = if (accent) Color(0xFFE0533D) else ClusterColors.OnBg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(2.dp),
        )
    }
}

@Composable
private fun ReadyBadge(isReady: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isReady) ClusterColors.GreenBadge else ClusterColors.Muted)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = "READY",
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun FuelBar(fuelPct: Int?, modifier: Modifier = Modifier) {
    val pct = (fuelPct ?: 60).coerceIn(0, 100)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("E", color = ClusterColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(2.dp))
                .background(ClusterColors.PanelStroke),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .background(ClusterColors.Accent)
                    .padding(vertical = 4.dp),
            )
        }
        Text("F", color = ClusterColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

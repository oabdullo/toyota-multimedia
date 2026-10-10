package com.toyota.cluster.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toyota.cluster.ClusterUiState
import com.toyota.cluster.ui.ClusterColors

/**
 * Right card: ADAS visualization (ECO badge + lane keeping car-in-lane
 * graphic), speed-arc gauge (0-30-60), instantaneous mpg, and trip
 * distance.
 *
 * The lane graphic + speed arc are pure Canvas. Real signals you'd wire in:
 *   - CRUISE_CONTROL_STATE                  -> ENGAGED/READY/OFF lights
 *   - ADAPTIVE_CRUISE_CONTROL_TARGET_SPEED  -> set point on the gauge
 *   - LANE_CENTERING_ASSIST_STATE           -> car icon centered vs. drift
 *   - LANE_KEEP_ASSIST_STATE                -> lane line color (white/red)
 */
@Composable
fun AdasCard(state: ClusterUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ClusterColors.PanelBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EcoBadge()
            CruiseChip(state.acc)
        }

        LaneGraphic(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            engaged = state.laneCenteringEngaged || state.acc == ClusterUiState.AccState.ENGAGED,
        )

        SpeedArc(
            speedMph = state.speedMph,
            mpg = state.mpg,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${state.tripMi} mi",
                color = ClusterColors.OnBg,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            ParkChip(parked = state.gear.label == "P")
        }
    }
}

@Composable
private fun EcoBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ClusterColors.GreenBadge.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = "ECO",
            color = ClusterColors.GreenBadge,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun CruiseChip(state: ClusterUiState.AccState) {
    val (label, color) = when (state) {
        ClusterUiState.AccState.OFF -> "ACC OFF" to ClusterColors.Muted
        ClusterUiState.AccState.READY -> "ACC RDY" to ClusterColors.OnBg
        ClusterUiState.AccState.ENGAGED -> "ACC ON" to ClusterColors.Accent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF132038))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** Two perspective lane lines with a car icon centered between them. */
@Composable
private fun LaneGraphic(modifier: Modifier = Modifier, engaged: Boolean) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val color = if (engaged) ClusterColors.Accent else ClusterColors.Muted
            drawLine(
                color = color,
                start = Offset(w * 0.15f, h),
                end = Offset(w * 0.40f, 0f),
                strokeWidth = 4.dp.toPx(),
            )
            drawLine(
                color = color,
                start = Offset(w * 0.85f, h),
                end = Offset(w * 0.60f, 0f),
                strokeWidth = 4.dp.toPx(),
            )
        }
        Icon(
            imageVector = Icons.Filled.DirectionsCar,
            contentDescription = null,
            tint = ClusterColors.OnBg,
            modifier = Modifier
                .size(36.dp)
                .padding(bottom = 4.dp),
        )
    }
}

/** Bottom: a 0-30-60 arc + MPG readout, mimicking the photo. */
@Composable
private fun SpeedArc(speedMph: Int, mpg: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val baseY = h * 0.65f
            // Tick marks
            val ticks = listOf("0" to 0.05f, "30" to 0.5f, "60" to 0.95f)
            ticks.forEach { (_, frac) ->
                drawLine(
                    color = ClusterColors.Muted,
                    start = Offset(w * frac, baseY - 4.dp.toPx()),
                    end = Offset(w * frac, baseY + 4.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            drawLine(
                color = ClusterColors.PanelStroke,
                start = Offset(0f, baseY),
                end = Offset(w, baseY),
                strokeWidth = 1.dp.toPx(),
            )
            // Filled portion proportional to current speed (cap 60)
            val fillFrac = (speedMph / 60f).coerceIn(0f, 1f)
            drawLine(
                color = ClusterColors.Accent,
                start = Offset(0f, baseY),
                end = Offset(w * fillFrac, baseY),
                strokeWidth = 3.dp.toPx(),
            )
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("0", color = ClusterColors.Muted, fontSize = 10.sp)
            Text("30", color = ClusterColors.Muted, fontSize = 10.sp)
            Text(
                text = "%.1f".format(mpg) + " MPG",
                color = ClusterColors.OnBg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ParkChip(parked: Boolean) {
    if (!parked) return
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFE0533D))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(
            text = "PARK",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

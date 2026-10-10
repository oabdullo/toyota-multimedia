package com.toyota.cluster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.cluster.ClusterViewModel
import com.toyota.cluster.ui.widgets.AdasCard
import com.toyota.cluster.ui.widgets.CompassCard
import com.toyota.cluster.ui.widgets.SpeedoCenter

/**
 * Three-pane cluster layout matching the reference photo:
 *
 *   [ CompassCard | SpeedoCenter (wide) | AdasCard ]
 *
 * Sized for the AAOS cluster virtual display (528 x 692 on the reference
 * AVD), but built fully responsive so it scales to OEM cluster sizes.
 */
@Composable
fun ClusterScreen() {
    val vm: ClusterViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClusterColors.Background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompassCard(
                state = state,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            SpeedoCenter(
                state = state,
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight(),
            )
            AdasCard(
                state = state,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

/**
 * Cluster-specific palette. We don't reuse the launcher's brown/blue/green
 * card colors here - the cluster is intentionally darker and more
 * monochromatic so the speedo and map dominate visually.
 */
internal object ClusterColors {
    val Background = Color(0xFF03060B)
    val PanelBg = Color(0xFF0B1320)
    val PanelStroke = Color(0xFF1B2536)
    val OnBg = Color(0xFFE6ECF5)
    val Muted = Color(0xFF7E8A9E)
    val Accent = Color(0xFF4CC9F0)
    val Warn = Color(0xFFFFB020)
    val GreenBadge = Color(0xFF34C759)
}

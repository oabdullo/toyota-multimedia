package com.toyota.launcher.feature.topstatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors

@Composable
fun TopStatusBar(modifier: Modifier = Modifier) {
    val vm: TopStatusViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    TopStatusBarContent(state = state, modifier = modifier)
}

@Composable
internal fun TopStatusBarContent(
    state: TopStatusState,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ToyotaColors.Background)
            .padding(horizontal = s.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // LEFT: notification bell
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ToyotaColors.SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.NotificationsNone, null,
                    tint = ToyotaColors.OnSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // RIGHT: brand pill, signal+carrier, bluetooth, mic, location, clock
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BrandPill()
            CarrierBadge(bars = state.signalBars, carrier = state.carrier)
            if (state.bluetoothOn) {
                Icon(Icons.Filled.Bluetooth, null, tint = ToyotaColors.OnSurface, modifier = Modifier.size(16.dp))
            }
            Icon(Icons.Filled.Mic, null,
                tint = if (state.microphoneActive) ToyotaColors.HvacAccentRed else ToyotaColors.OnSurface,
                modifier = Modifier.size(16.dp))
            if (state.locationEnabled) {
                Icon(Icons.Filled.LocationOn, null, tint = ToyotaColors.OnSurface, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = state.timeText,
                color = ToyotaColors.OnSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun BrandPill() {
    // Mimics the Toyota mark + "DCM" text in the reference screenshot.
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(ToyotaColors.SurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(ToyotaColors.HvacAccentRed),
            )
        }
    }
}

@Composable
private fun CarrierBadge(bars: Int, carrier: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.SignalCellular4Bar, null,
            tint = ToyotaColors.OnSurface.copy(alpha = if (bars >= 3) 1f else 0.4f),
            modifier = Modifier
                .size(14.dp)
                .fillMaxHeight(),
        )
        if (carrier.isNotEmpty()) {
            Text(
                text = carrier,
                color = ToyotaColors.OnSurfaceMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

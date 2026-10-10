package com.toyota.launcher.feature.uxrestrictions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.launcher.core.design.ToyotaColors

/**
 * Wraps a screen in a UX restrictions observer. While the vehicle is in
 * motion (and the system reports the LIMIT_CONTENT or NO_KEYBOARD
 * restriction) we render a banner. We deliberately do NOT block touches -
 * HVAC and play/pause are explicitly allowed by the AAOS distraction
 * guidelines. Individual features may also disable text input on their own.
 */
@Composable
fun UxRestrictionsScrim(content: @Composable () -> Unit) {
    val vm: UxRestrictionsViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (state.isDriving) {
            DrivingBanner(snapshot = state)
        }
    }
}

@Composable
private fun BoxScope.DrivingBanner(snapshot: UxRestrictionsSnapshot) {
    Row(
        modifier = Modifier
            .padding(top = 64.dp, end = 16.dp)
            .align(Alignment.TopEnd)
            .clip(RoundedCornerShape(12.dp))
            .background(ToyotaColors.HvacAccentRed.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.WarningAmber, null,
            tint = Color.White,
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(
            text = bannerText(snapshot),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun bannerText(s: UxRestrictionsSnapshot): String {
    val bits = buildList {
        if (s.noKeyboard) add("no keyboard")
        if (s.noVideo) add("no video")
        if (s.limitContent) add("limited content")
    }
    return if (bits.isEmpty()) "Driving" else "Driving (${bits.joinToString()})"
}

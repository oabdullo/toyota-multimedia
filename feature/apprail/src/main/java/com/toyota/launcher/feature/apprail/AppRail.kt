package com.toyota.launcher.feature.apprail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors

@Composable
fun AppRail(modifier: Modifier = Modifier) {
    val vm: AppRailViewModel = hiltViewModel()
    AppRailContent(
        slots = RailSlots.primary,
        onLaunch = vm::launch,
        modifier = modifier,
    )
}

@Composable
internal fun AppRailContent(
    slots: List<RailSlot>,
    onLaunch: (RailSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(ToyotaColors.AppRailBackground)
            .padding(vertical = s.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(s.lg),
    ) {
        slots.forEach { slot ->
            RailIcon(
                icon = slot.icon,
                isActive = slot.isActive,
                onClick = { onLaunch(slot) },
            )
            // Visual divider between Home and the rest, mirroring the screenshot.
            if (slot.isActive) {
                Spacer(
                    modifier = Modifier
                        .width(24.dp)
                        .padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun RailIcon(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isActive) ToyotaColors.AppRailIconActiveBg else ToyotaColors.AppRailBackground
    val tint = if (isActive) ToyotaColors.AppRailIconActive else ToyotaColors.AppRailIconInactive
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

package com.toyota.launcher.feature.drivemode

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.launcher.core.design.BigCard
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors

@Composable
fun DriveModeCard(modifier: Modifier = Modifier) {
    val vm: DriveModeViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    DriveModeCardContent(
        state = state,
        onCycleMode = vm::cycleMode,
        modifier = modifier,
    )
}

@Composable
internal fun DriveModeCardContent(
    state: DriveModeUiState,
    onCycleMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    BigCard(backgroundColor = ToyotaColors.DriveModeBg, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Drive mode",
                    color = ToyotaColors.DriveModeOnBg,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = state.gear.label,
                    color = ToyotaColors.DriveModeOnBg.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }

            // Vehicle "render" - drop a PNG/JPG/WebP at
            //   feature/drivemode/src/main/res/drawable-nodpi/vehicle_render.png
            // and it will replace the placeholder car icon automatically.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val ctx = LocalContext.current
                val resId = remember {
                    ctx.resources.getIdentifier(
                        "vehicle_render", "drawable", ctx.packageName,
                    )
                }
                if (resId != 0) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = "Vehicle",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = ToyotaColors.DriveModeOnBg,
                        modifier = Modifier.size(120.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ToyotaColors.DriveModeChip)
                    .clickable { onCycleMode() }
                    .padding(vertical = s.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.mode.label,
                    color = ToyotaColors.DriveModeOnBg,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

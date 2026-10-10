package com.toyota.launcher.feature.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.launcher.core.design.BigCard
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors

@Composable
fun NowPlayingCard(modifier: Modifier = Modifier) {
    val vm: MediaViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    NowPlayingCardContent(
        state = state,
        onPlayPause = vm::togglePlay,
        onPrev = vm::skipPrev,
        onNext = vm::skipNext,
        modifier = modifier,
    )
}

@Composable
internal fun NowPlayingCardContent(
    state: MediaState,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    BigCard(backgroundColor = ToyotaColors.MediaBg, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Radio, null,
                        tint = ToyotaColors.MediaOnBg,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = state.subtitle.ifBlank { "FM" },
                        color = ToyotaColors.MediaOnBg,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(vertical = s.sm)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ToyotaColors.MediaArtBg),
                contentAlignment = Alignment.Center,
            ) {
                val art = state.albumArt
                if (art != null) {
                    Image(
                        bitmap = art.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    // Fallback "logo" - mimics the green 88.1 indie circle
                    Text(
                        text = "88.1\nindie",
                        color = ToyotaColors.MediaBg,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    )
                }
            }

            Text(
                text = state.title,
                color = ToyotaColors.MediaOnBg,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MediaButton(
                    icon = Icons.Filled.SkipPrevious,
                    enabled = state.canSkipPrev,
                    onClick = onPrev,
                )
                MediaButton(
                    icon = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    enabled = true,
                    onClick = onPlayPause,
                    isPrimary = true,
                )
                MediaButton(
                    icon = Icons.Filled.SkipNext,
                    enabled = state.canSkipNext,
                    onClick = onNext,
                )
            }
        }
    }
}

@Composable
private fun MediaButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
) {
    val size = if (isPrimary) 56.dp else 44.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isPrimary) Color.Black.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ToyotaColors.MediaOnBg.copy(alpha = if (enabled) 1f else 0.4f),
            modifier = Modifier.size(if (isPrimary) 30.dp else 24.dp),
        )
    }
}

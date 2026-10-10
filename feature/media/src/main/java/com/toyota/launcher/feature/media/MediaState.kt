package com.toyota.launcher.feature.media

import android.graphics.Bitmap

data class MediaState(
    val hasSession: Boolean = false,
    val title: String = "KNTU 88.1",
    val subtitle: String = "FM",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val canSkipPrev: Boolean = true,
    val canSkipNext: Boolean = true,
    val artHexBackground: Long = 0xFF2E8F4F,
)

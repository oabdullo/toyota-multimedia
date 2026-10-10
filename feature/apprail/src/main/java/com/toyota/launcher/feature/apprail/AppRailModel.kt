package com.toyota.launcher.feature.apprail

import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The five primary destinations on the left rail. Order matches the
 * reference screenshot: Home, Nav, Music, Phone, Settings.
 *
 * Each slot carries an ordered list of [Intent]s; [AppRailViewModel] tries
 * them in turn and picks the first one that resolves to an installed
 * activity. This lets us declare a canonical "preferred" intent plus
 * fallbacks for OEM / emulator variations (e.g. the AAOS MapsPlaceholder
 * stub when Google Maps isn't installed).
 *
 * Home is the active state since we ARE the launcher.
 */
data class RailSlot(
    val id: String,
    val icon: ImageVector,
    val intents: List<Intent>,
    val isActive: Boolean = false,
)

object RailSlots {
    val Home = RailSlot(
        id = "home",
        icon = Icons.Filled.Home,
        intents = emptyList(), // we are home
        isActive = true,
    )

    /**
     * Nav: tries the user's default maps app (CATEGORY_APP_MAPS), then any
     * activity that handles geo: URIs (Google Maps, Waze, the AAOS
     * MapsPlaceholder stub, etc.), then Google Maps by package name.
     */
    val Nav = RailSlot(
        id = "nav",
        icon = Icons.Filled.Map,
        intents = listOf(
            // The canonical "open the user's preferred maps app" intent.
            // makeMainSelectorActivity wraps ACTION_MAIN + CATEGORY_APP_MAPS.
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MAPS),
            // Generic geo URI - resolves to whatever activity handles it
            // (on stock AAOS this is com.android.car.mapsplaceholder).
            Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")),
            Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")),
            // Last resort: explicit Google Maps package launch.
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage("com.google.android.apps.maps"),
        ),
    )

    val Music = RailSlot(
        id = "music",
        icon = Icons.Filled.MusicNote,
        intents = listOf(
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC),
            Intent("android.intent.action.MUSIC_PLAYER"),
        ),
    )

    val Phone = RailSlot(
        id = "phone",
        icon = Icons.Filled.Phone,
        intents = listOf(
            Intent(Intent.ACTION_DIAL),
        ),
    )

    val Settings = RailSlot(
        id = "settings",
        icon = Icons.Filled.Settings,
        intents = listOf(
            Intent(AndroidSettings.ACTION_SETTINGS),
        ),
    )

    val primary = listOf(Home, Nav, Music, Phone, Settings)
}

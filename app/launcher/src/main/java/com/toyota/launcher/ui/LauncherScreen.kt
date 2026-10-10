package com.toyota.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors
import com.toyota.launcher.feature.apprail.AppRail
import com.toyota.launcher.feature.drivemode.DriveModeCard
import com.toyota.launcher.feature.hvac.HvacStrip
import com.toyota.launcher.feature.media.NowPlayingCard
import com.toyota.launcher.feature.topstatus.TopStatusBar
import com.toyota.launcher.feature.uxrestrictions.UxRestrictionsScrim
import com.toyota.launcher.feature.weather.WeatherCard

/**
 * Top-level home dashboard. Layout, top to bottom:
 *   - TopStatusBar  (clock, signal, BT, mic, location, notification bell)
 *   - Row {
 *       AppRail (vertical)
 *       3-up card grid: DriveMode, Weather, NowPlaying
 *     }
 *   - HvacStrip (temp, seat heat, fan, defrost, sync)
 *
 * Sized for the AAOS reference 1024x600 head unit but stretches to fill.
 */
@Composable
fun LauncherScreen() {
    val s = LocalSpacing.current
    UxRestrictionsScrim {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ToyotaColors.Background),
        ) {
            TopStatusBar(modifier = Modifier.height(s.topBarHeight))

            Row(modifier = Modifier.weight(1f)) {
                AppRail(modifier = Modifier.width(s.appRailWidth))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = s.lg, vertical = s.md),
                    horizontalArrangement = Arrangement.spacedBy(s.cardGap),
                ) {
                    DriveModeCard(modifier = Modifier.weight(1f).fillMaxSize())
                    WeatherCard(modifier = Modifier.weight(1f).fillMaxSize())
                    NowPlayingCard(modifier = Modifier.weight(1f).fillMaxSize())
                }
            }

            HvacStrip(modifier = Modifier.height(s.hvacStripHeight))

            // Hairline below HVAC strip mimicking reference screenshot
            Spacer(modifier = Modifier.height(s.xs).background(ToyotaColors.OutlineSubtle))
        }
    }
}

/** Empty box used by previews when a real composable is not yet wired. */
@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(ToyotaColors.Background)) { content() }
}

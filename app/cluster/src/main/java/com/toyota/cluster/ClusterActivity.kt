package com.toyota.cluster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.toyota.cluster.ui.ClusterScreen
import com.toyota.launcher.core.design.ToyotaLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The cluster Activity. Intended to be launched on the AAOS cluster display
 * (typically display id 3 on the reference emulator) via
 *   `adb shell am start --display 3 -n com.toyota.cluster/.ClusterActivity`
 * or programmatically with `ActivityOptions.setLaunchDisplayId(...)`.
 *
 * The display itself is `touch NONE` (see `dumpsys display`), so we don't
 * register any input handlers - the cluster is informational only.
 */
@AndroidEntryPoint
class ClusterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            ToyotaLauncherTheme {
                ClusterScreen()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowInsetsControllerCompat(window, window.decorView)
                .hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

package com.toyota.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.toyota.launcher.core.design.ToyotaLauncherTheme
import com.toyota.launcher.ui.LauncherScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Launcher entry point. Declared with category HOME so the system can pick it
 * via `cmd package set-home-activity com.toyota.launcher/.MainActivity`.
 *
 * We hide the system bars and own the entire canvas, drawing our own top
 * status strip and bottom HVAC strip. The trade-off (vs. building a custom
 * CarSystemUI APK that owns the real system bars) is documented in the README.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                LauncherScreen()
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

    /** Don't let HOME key navigate away from us - we are HOME. */
    override fun onBackPressed() = Unit
}

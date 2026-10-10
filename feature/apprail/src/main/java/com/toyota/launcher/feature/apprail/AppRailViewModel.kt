package com.toyota.launcher.feature.apprail

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Resolves which apps are available on the device for the rail's overflow
 * drawer (3rd-party automotive apps, e.g. Spotify, Pandora, etc.). The
 * primary slots are static.
 */
@HiltViewModel
class AppRailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    /**
     * Walk the slot's intent chain and start the first one that resolves
     * to an installed Activity. We start from Application context so
     * FLAG_ACTIVITY_NEW_TASK is required - we add it uniformly here so the
     * slot definitions don't have to remember.
     */
    fun launch(slot: RailSlot) {
        if (slot.intents.isEmpty()) return // home is a no-op
        val pm = context.packageManager
        for (raw in slot.intents) {
            val intent = Intent(raw).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(pm) == null) continue
            try {
                context.startActivity(intent)
                return
            } catch (t: Throwable) {
                Log.w(TAG, "launch(${slot.id}) failed for $intent", t)
            }
        }
        Log.w(TAG, "launch(${slot.id}) - no intent in chain resolved")
    }

    fun overflowApps(): List<ResolvedApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            addCategory(CATEGORY_AUTOMOTIVE_APP)
        }
        val flags = PackageManager.ResolveInfoFlags.of(0L)
        return pm.queryIntentActivities(intent, flags)
            .map {
                ResolvedApp(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    iconResId = it.iconResource,
                )
            }
            .filter { it.packageName != context.packageName } // exclude ourselves
            .sortedBy { it.label }
    }

    data class ResolvedApp(
        val label: String,
        val packageName: String,
        val iconResId: Int,
    )

    companion object {
        const val CATEGORY_AUTOMOTIVE_APP = "android.intent.category.LAUNCHER"
        private const val TAG = "AppRailViewModel"
    }
}

package com.toyota.launcher.feature.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Wraps [MediaSessionManager.getActiveSessions] into a Flow. Requires the
 * privileged android.permission.MEDIA_CONTENT_CONTROL granted to us via
 * /system/priv-app/ + privapp-allowlist (see install-priv.sh).
 *
 * On AAOS, multiple media apps can hold sessions; we surface whichever the
 * session manager reports as the *highest priority* active one (typically
 * the playing one).
 */
@Singleton
class MediaSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Note: getActiveSessions takes a NotificationListenerService component
     * for non-system callers. Because we hold MEDIA_CONTENT_CONTROL we may
     * pass null. If the perm grant is missing this will throw
     * SecurityException; we catch and emit an empty state.
     */
    fun observeTopSession(): Flow<MediaController?> = callbackFlow {
        val msm = context.getSystemService(MediaSessionManager::class.java)
        if (msm == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val emit = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            trySend(controllers?.firstOrNull())
        }

        try {
            // null = system app or holder of MEDIA_CONTENT_CONTROL
            val initial = msm.getActiveSessions(null as ComponentName?)
            trySend(initial.firstOrNull())
            msm.addOnActiveSessionsChangedListener(emit, null)
        } catch (sec: SecurityException) {
            // Expected when the launcher isn't installed under /system/priv-app -
            // log at INFO without the stack so debug logcat stays usable. The
            // session falls back to a mock card; install via
            // tools/scripts/install-priv.sh to enable real Now Playing.
            Log.i(
                TAG,
                "No MEDIA_CONTENT_CONTROL grant (${sec.message}); falling back " +
                    "to empty session. Install to /system/priv-app via " +
                    "tools/scripts/install-priv.sh to mirror real media sessions.",
            )
            trySend(null)
        }

        awaitClose {
            runCatching { msm.removeOnActiveSessionsChangedListener(emit) }
        }
    }

    fun snapshot(controller: MediaController?): MediaState {
        if (controller == null) return MediaState(hasSession = false)
        val md = controller.metadata
        val pb = controller.playbackState
        return MediaState(
            hasSession = true,
            title = md?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                ?: md?.getString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: "Unknown",
            subtitle = md?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                ?: md?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM)
                ?: "",
            albumArt = md?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: md?.getBitmap(android.media.MediaMetadata.METADATA_KEY_DISPLAY_ICON),
            isPlaying = pb?.state == PlaybackState.STATE_PLAYING,
            canSkipPrev = pb?.actions?.and(PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L,
            canSkipNext = pb?.actions?.and(PlaybackState.ACTION_SKIP_TO_NEXT) != 0L,
        )
    }

    private companion object { const val TAG = "MediaSessionRepository" }
}

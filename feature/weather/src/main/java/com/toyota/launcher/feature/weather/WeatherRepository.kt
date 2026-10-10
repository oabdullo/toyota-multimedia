package com.toyota.launcher.feature.weather

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.delay

/**
 * Polls the weather mock ContentProvider every 15 minutes (matches typical
 * weather-card refresh cadences). Falls back to the canned [WeatherState]
 * default if the provider isn't installed.
 */
@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observe(): Flow<WeatherState> = flow {
        while (true) {
            emit(readOnce())
            delay(REFRESH_INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)

    private fun readOnce(): WeatherState {
        val cursor = try {
            context.contentResolver.query(URI_CURRENT, null, null, null, null)
        } catch (t: Throwable) {
            Log.w(TAG, "Weather provider not reachable, using default", t)
            null
        } ?: return WeatherState()

        return cursor.use {
            if (!it.moveToFirst()) return WeatherState()
            WeatherState(
                location = it.getString(it.getColumnIndexOrThrow("location")),
                tempF = it.getInt(it.getColumnIndexOrThrow("temp_f")),
                tempC = it.getInt(it.getColumnIndexOrThrow("temp_c")),
                condition = it.getString(it.getColumnIndexOrThrow("condition")),
                icon = it.getString(it.getColumnIndexOrThrow("icon")),
                attribution = it.getString(it.getColumnIndexOrThrow("attribution")),
            )
        }
    }

    private companion object {
        const val TAG = "WeatherRepository"
        const val REFRESH_INTERVAL_MS = 15 * 60 * 1000L
        val URI_CURRENT: Uri = Uri.parse("content://com.toyota.weather/current")
    }
}

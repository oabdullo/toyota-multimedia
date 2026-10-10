package com.toyota.weather

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Mock AccuWeather-style provider for the launcher's weather card.
 * In a real OEM stack this would proxy to a cloud weather backend keyed on
 * the vehicle's GPS. We just return the canned reading to match the
 * reference screenshot.
 *
 * URI:        content://com.toyota.weather/current
 * Columns:    location, temp_f, temp_c, condition, icon, attribution
 */
class WeatherProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        if (matcher.match(uri) != CURRENT) return null
        val cols = arrayOf(
            COL_LOCATION, COL_TEMP_F, COL_TEMP_C, COL_CONDITION, COL_ICON, COL_ATTRIBUTION,
        )
        return MatrixCursor(cols).apply {
            addRow(arrayOf("Plano", 83, 28, "Sunny", "sun", "AccuWeather"))
        }
    }

    override fun getType(uri: Uri): String? = when (matcher.match(uri)) {
        CURRENT -> "vnd.android.cursor.item/vnd.com.toyota.weather.current"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val AUTHORITY = "com.toyota.weather"
        const val COL_LOCATION = "location"
        const val COL_TEMP_F = "temp_f"
        const val COL_TEMP_C = "temp_c"
        const val COL_CONDITION = "condition"
        const val COL_ICON = "icon"
        const val COL_ATTRIBUTION = "attribution"

        private const val CURRENT = 1
        private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "current", CURRENT)
        }

        val URI_CURRENT: Uri = Uri.parse("content://$AUTHORITY/current")
    }
}

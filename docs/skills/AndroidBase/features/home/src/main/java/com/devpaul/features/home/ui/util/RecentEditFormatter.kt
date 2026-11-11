package com.devpaul.features.home.ui.util

import android.content.Context
import com.devpaul.features.home.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatRecentEdit(
    context: Context,
    updatedAt: Long,
    now: Long = System.currentTimeMillis(),
): String {
    if (updatedAt <= 0L) return context.getString(R.string.home_edited_recently)
    val elapsedMinutes = ((now - updatedAt).coerceAtLeast(0L) / 60_000L).toInt()
    return when {
        elapsedMinutes < 1 -> context.getString(R.string.home_edited_now)
        elapsedMinutes == 1 -> context.getString(R.string.home_edited_one_minute)
        elapsedMinutes < 60 -> context.getString(R.string.home_edited_minutes, elapsedMinutes)
        elapsedMinutes < 120 -> context.getString(R.string.home_edited_one_hour)
        elapsedMinutes < 1_440 -> context.getString(R.string.home_edited_hours, elapsedMinutes / 60)
        elapsedMinutes < 2_880 -> context.getString(R.string.home_edited_one_day)
        elapsedMinutes < 10_080 -> context.getString(R.string.home_edited_days, elapsedMinutes / 1_440)
        else -> context.getString(
            R.string.home_edited_date,
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(updatedAt)),
        )
    }
}

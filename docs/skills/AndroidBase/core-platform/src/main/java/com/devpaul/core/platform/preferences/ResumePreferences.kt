package com.devpaul.core.platform.preferences

import android.content.Context
import androidx.core.content.edit

object ResumePreferences {
    fun autoSaveEnabled(context: Context): Boolean = preferences(context).getBoolean(AUTO_SAVE_KEY, true)

    fun setAutoSaveEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit { putBoolean(AUTO_SAVE_KEY, enabled) }
    }

    private fun preferences(context: Context) = context.getSharedPreferences(
        "resume_preferences",
        Context.MODE_PRIVATE,
    )

    private const val AUTO_SAVE_KEY = "auto_save_enabled"
}

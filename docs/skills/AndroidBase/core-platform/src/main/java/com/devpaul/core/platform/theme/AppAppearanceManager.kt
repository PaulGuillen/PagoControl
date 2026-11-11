package com.devpaul.core.platform.theme

import android.content.Context
import androidx.core.content.edit

object AppAppearanceManager {
    fun current(context: Context): AppAppearance = AppAppearance.fromPreference(
        preferences(context).getString(APPEARANCE_KEY, null),
    )

    fun set(context: Context, appearance: AppAppearance) {
        preferences(context).edit { putString(APPEARANCE_KEY, appearance.preferenceValue) }
    }

    private fun preferences(context: Context) = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private const val PREFERENCES_NAME = "app_appearance"
    private const val APPEARANCE_KEY = "appearance"
}

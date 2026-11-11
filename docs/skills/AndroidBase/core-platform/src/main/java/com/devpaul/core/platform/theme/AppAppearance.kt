package com.devpaul.core.platform.theme

enum class AppAppearance(val preferenceValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    fun isDarkTheme(systemDarkTheme: Boolean): Boolean = when (this) {
        System -> systemDarkTheme
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromPreference(value: String?): AppAppearance =
            entries.firstOrNull { it.preferenceValue == value } ?: System
    }
}

package com.devpaul.core.platform.locale

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object AppLanguageManager {
    fun current(context: Context): AppLanguage {
        val savedTag = preferences(context).getString(LANGUAGE_KEY, AppLanguage.Spanish.languageTag)
        return AppLanguage.entries.firstOrNull { it.languageTag == savedTag } ?: AppLanguage.Spanish
    }

    fun set(context: Context, language: AppLanguage) {
        preferences(context).edit(commit = true) { putString(LANGUAGE_KEY, language.languageTag) }
    }

    fun wrap(
        context: Context,
        language: AppLanguage = current(context),
    ): Context {
        val locale = Locale.forLanguageTag(language.languageTag)
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    private fun preferences(context: Context) = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private const val PREFERENCES_NAME = "app_language"
    private const val LANGUAGE_KEY = "language_tag"
}

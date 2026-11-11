package com.devpaul.features.settings.ui.util

import androidx.annotation.StringRes
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.features.settings.R

@StringRes
internal fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.Spanish -> R.string.settings_language_spanish
    AppLanguage.English -> R.string.settings_language_english
}

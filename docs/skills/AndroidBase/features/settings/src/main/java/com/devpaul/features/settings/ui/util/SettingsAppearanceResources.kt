package com.devpaul.features.settings.ui.util

import androidx.annotation.StringRes
import com.devpaul.core.platform.theme.AppAppearance
import com.devpaul.features.settings.R

@StringRes
internal fun AppAppearance.labelRes(): Int = when (this) {
    AppAppearance.System -> R.string.settings_system
    AppAppearance.Light -> R.string.settings_appearance_light
    AppAppearance.Dark -> R.string.settings_appearance_dark
}

@StringRes
internal fun AppAppearance.subtitleRes(): Int = when (this) {
    AppAppearance.System -> R.string.settings_appearance_subtitle
    AppAppearance.Light -> R.string.settings_appearance_light_subtitle
    AppAppearance.Dark -> R.string.settings_appearance_dark_subtitle
}

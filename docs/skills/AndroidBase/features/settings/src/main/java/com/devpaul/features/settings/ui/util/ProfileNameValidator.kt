package com.devpaul.features.settings.ui.util

internal const val MAX_PROFILE_NAME_LENGTH = 60

internal fun isValidProfileName(name: String): Boolean = name.trim().length >= 2

package com.devpaul.features.settings.ui.util

internal fun profileInitials(name: String): String = name
    .trim()
    .split(Regex("\\s+"))
    .filter(String::isNotBlank)
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .joinToString("")
    .ifBlank { "CV" }

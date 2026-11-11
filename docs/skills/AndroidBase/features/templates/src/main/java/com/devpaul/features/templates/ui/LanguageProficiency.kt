package com.devpaul.features.templates.ui

import java.util.Locale

internal fun languageProficiency(level: String): Float = when (level.trim().lowercase(Locale.ROOT)) {
    "nativo", "native" -> 1f
    "avanzado", "advanced", "fluido", "fluent" -> 0.85f
    "intermedio", "intermediate" -> 0.65f
    else -> 0.4f
}

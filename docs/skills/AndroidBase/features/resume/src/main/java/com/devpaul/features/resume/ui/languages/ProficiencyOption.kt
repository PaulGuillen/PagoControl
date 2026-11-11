package com.devpaul.features.resume.ui.languages

import androidx.annotation.StringRes
import com.devpaul.features.resume.R

internal enum class ProficiencyOption(val value: String, val englishValue: String, @StringRes val label: Int) {
    Basic("Básico", "Basic", R.string.ui_basic_303ce5),
    Intermediate("Intermedio", "Intermediate", R.string.ui_intermediate_66ca7e),
    Advanced("Avanzado", "Advanced", R.string.ui_advanced_980a92),
    Native("Nativo", "Native", R.string.ui_native_fffb17),
}

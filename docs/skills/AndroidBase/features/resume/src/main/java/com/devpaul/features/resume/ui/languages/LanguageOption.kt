package com.devpaul.features.resume.ui.languages

import androidx.annotation.StringRes
import com.devpaul.features.resume.R

internal enum class LanguageOption(val value: String, val englishValue: String, @StringRes val label: Int) {
    Spanish("Español", "Spanish", R.string.ui_spanish_2001ca),
    English("Inglés", "English", R.string.ui_english_3d9be9),
    Portuguese("Portugués", "Portuguese", R.string.ui_portuguese_f19af7),
    French("Francés", "French", R.string.ui_french_ff222e),
    German("Alemán", "German", R.string.ui_german_d52eb4),
}

package com.devpaul.features.resume.ui.languages

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

internal val availableLanguages = LanguageOption.entries.map(LanguageOption::value)
internal val availableLevels = ProficiencyOption.entries.map(ProficiencyOption::value)

private fun languageOption(value: String): LanguageOption? = LanguageOption.entries.firstOrNull {
    value.equals(it.value, ignoreCase = true) || value.equals(it.englishValue, ignoreCase = true)
}

private fun proficiencyOption(value: String): ProficiencyOption? = ProficiencyOption.entries.firstOrNull {
    value.equals(it.value, ignoreCase = true) || value.equals(it.englishValue, ignoreCase = true)
}

internal fun sameLanguage(first: String, second: String): Boolean =
    first.equals(second, ignoreCase = true) ||
        languageOption(first)?.let { it == languageOption(second) } == true

internal fun sameProficiency(first: String, second: String): Boolean =
    first.equals(second, ignoreCase = true) ||
        proficiencyOption(first)?.let { it == proficiencyOption(second) } == true

@Composable
internal fun languageLabel(value: String): String = languageOption(value)?.let { stringResource(it.label) } ?: value

@Composable
internal fun proficiencyLabel(value: String): String = proficiencyOption(value)?.let { stringResource(it.label) } ?: value

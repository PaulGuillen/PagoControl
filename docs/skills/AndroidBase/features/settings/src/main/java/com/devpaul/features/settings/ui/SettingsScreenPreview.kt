package com.devpaul.features.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.theme.AppAppearance

@Preview(name = "Settings Light", showBackground = true, heightDp = 900)
@Composable
private fun SettingsLightPreview() {
    CvFreeTheme(darkTheme = false) {
        SettingsScreen({}, {}, {}, "Elena Vargas", "elena@example.com", { null }, AppLanguage.Spanish, {}, AppAppearance.Light, {}, true, {}, true, {})
    }
}

@Preview(name = "Settings Dark", showBackground = true, heightDp = 900)
@Composable
private fun SettingsDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        SettingsScreen({}, {}, {}, "Elena Vargas", "elena@example.com", { null }, AppLanguage.Spanish, {}, AppAppearance.Dark, {}, true, {}, true, {})
    }
}

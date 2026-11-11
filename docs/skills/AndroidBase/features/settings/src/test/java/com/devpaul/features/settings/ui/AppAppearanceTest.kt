package com.devpaul.features.settings.ui

import com.devpaul.core.platform.theme.AppAppearance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppAppearanceTest {
    @Test
    fun `system appearance follows device theme`() {
        assertFalse(AppAppearance.System.isDarkTheme(systemDarkTheme = false))
        assertTrue(AppAppearance.System.isDarkTheme(systemDarkTheme = true))
    }

    @Test
    fun `manual appearance overrides device theme`() {
        assertFalse(AppAppearance.Light.isDarkTheme(systemDarkTheme = false))
        assertFalse(AppAppearance.Light.isDarkTheme(systemDarkTheme = true))
        assertTrue(AppAppearance.Dark.isDarkTheme(systemDarkTheme = false))
        assertTrue(AppAppearance.Dark.isDarkTheme(systemDarkTheme = true))
    }

    @Test
    fun `saved appearances can be restored`() {
        AppAppearance.entries.forEach { appearance ->
            assertEquals(appearance, AppAppearance.fromPreference(appearance.preferenceValue))
        }
    }

    @Test
    fun `missing and unknown preferences use system appearance`() {
        assertEquals(AppAppearance.System, AppAppearance.fromPreference(null))
        assertEquals(AppAppearance.System, AppAppearance.fromPreference("unknown"))
    }
}

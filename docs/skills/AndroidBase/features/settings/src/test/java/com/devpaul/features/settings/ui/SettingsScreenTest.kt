package com.devpaul.features.settings.ui

import com.devpaul.features.settings.ui.util.profileInitials
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsScreenTest {

    @Test
    fun `profile initials use the first two words`() {
        assertEquals("EV", profileInitials("Elena Vargas Salazar"))
    }

    @Test
    fun `profile initials handle empty names`() {
        assertEquals("CV", profileInitials("   "))
    }
}

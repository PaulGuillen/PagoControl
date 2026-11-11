package com.devpaul.features.resume.ui.languages

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageLabelsTest {
    @Test
    fun `imported English values match their stored Spanish options`() {
        assertTrue(sameLanguage("Español", "Spanish"))
        assertTrue(sameLanguage("English", "Inglés"))
        assertTrue(sameProficiency("Nativo", "Native"))
        assertTrue(sameProficiency("INTERMEDIATE", "Intermedio"))
        assertFalse(sameLanguage("Custom language", "Other language"))
        assertFalse(sameProficiency("Básico", "Native"))
    }
}

package com.devpaul.features.templates.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageProficiencyTest {
    @Test
    fun `Spanish and English levels produce the same bars`() {
        listOf(
            Triple("Nativo", "Native", 1f),
            Triple("Avanzado", "Advanced", 0.85f),
            Triple("Intermedio", "Intermediate", 0.65f),
            Triple("Básico", "Basic", 0.4f),
        ).forEach { (spanish, english, expected) ->
            assertEquals(expected, languageProficiency(spanish), 0f)
            assertEquals(expected, languageProficiency(" ${english.uppercase()} "), 0f)
        }
    }
}

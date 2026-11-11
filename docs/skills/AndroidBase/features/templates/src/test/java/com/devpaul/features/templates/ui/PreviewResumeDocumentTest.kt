package com.devpaul.features.templates.ui

import com.devpaul.core.platform.locale.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewResumeDocumentTest {
    @Test
    fun `sample content follows the requested language`() {
        val spanish = previewDocument(AppLanguage.Spanish)
        val english = previewDocument(AppLanguage.English)

        assertEquals("Desarrolladora Android", spanish.contact.professionalTitle)
        assertEquals("Android Developer", english.contact.professionalTitle)
        assertTrue(english.summary.startsWith("Senior Android developer"))
        assertEquals("Portfolio", english.links.last().name)
        assertEquals("Systems Engineering", english.education.single().degree)
        assertEquals("elena.vargas@example.com", english.contact.email)
        assertEquals(spanish.contact.email, english.contact.email)
    }
}

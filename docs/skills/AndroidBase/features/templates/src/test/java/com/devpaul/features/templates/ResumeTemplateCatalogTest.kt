package com.devpaul.features.templates

import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.features.templates.pdf.ResumePdfExporter
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumeTemplateCatalogTest {

    @Test
    fun `resolves Budapest by current and legacy identifiers`() {
        assertEquals(ResumeTemplateCatalog.BUDAPEST_ID, ResumeDocument().templateName)
        assertEquals(
            ResumeTemplateCatalog.BUDAPEST_ID,
            ResumeTemplateCatalog.find(ResumeTemplateCatalog.BUDAPEST_ID).id,
        )
        assertEquals(
            ResumeTemplateCatalog.BUDAPEST_ID,
            ResumeTemplateCatalog.find("classic_sidebar").id,
        )
        assertEquals(
            ResumeTemplateCatalog.BUDAPEST_ID,
            ResumeTemplateCatalog.find("Clásico Sidebar").id,
        )
        assertEquals(ResumeTemplateCatalog.BUDAPEST_ID, ResumeTemplateCatalog.find("Budapest").id)
    }

    @Test
    fun `resolves Riga by current and legacy identifiers`() {
        assertEquals(
            ResumeTemplateCatalog.RIGA_ID,
            ResumeTemplateCatalog.find(ResumeTemplateCatalog.RIGA_ID).id,
        )
        assertEquals(
            ResumeTemplateCatalog.RIGA_ID,
            ResumeTemplateCatalog.find("editorial_line").id,
        )
        assertEquals(
            ResumeTemplateCatalog.RIGA_ID,
            ResumeTemplateCatalog.find("Editorial Line").id,
        )
        assertEquals(
            ResumeTemplateCatalog.RIGA_ID,
            ResumeTemplateCatalog.find("Línea editorial").id,
        )
        assertEquals(ResumeTemplateCatalog.RIGA_ID, ResumeTemplateCatalog.find("Riga").id)
    }

    @Test
    fun `resolves prague template by id and names`() {
        assertEquals(
            ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID,
            ResumeTemplateCatalog.find(ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID).id,
        )
        assertEquals(
            ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID,
            ResumeTemplateCatalog.find("Prague").id,
        )
        assertEquals(
            ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID,
            ResumeTemplateCatalog.find("Praga").id,
        )
    }

    @Test
    fun `resolves tokyo template by id and names`() {
        assertEquals(
            ResumeTemplateCatalog.TOKYO_COMPACT_ID,
            ResumeTemplateCatalog.find(ResumeTemplateCatalog.TOKYO_COMPACT_ID).id,
        )
        assertEquals(
            ResumeTemplateCatalog.TOKYO_COMPACT_ID,
            ResumeTemplateCatalog.find("Tokyo").id,
        )
        assertEquals(
            ResumeTemplateCatalog.TOKYO_COMPACT_ID,
            ResumeTemplateCatalog.find("Tokio").id,
        )
    }

    @Test
    fun `creates a portable pdf file name`() {
        val resume = ResumeDocument(
            contact = ResumeContact(name = "Paúl", lastName = "Acuña Ramos"),
        )

        assertEquals("CV_Paul_Acuna_Ramos.pdf", ResumePdfExporter.suggestedFileName(resume))
    }
}

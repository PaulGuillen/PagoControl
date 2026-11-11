package com.devpaul.features.home.ui.model

import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumeProgressTest {

    @Test
    fun `empty resume has zero percent and suggests contact information`() {
        val progress = resumeProgress(ResumeDocument())

        assertEquals(0, progress.percent)
        assertEquals(ResumeProgressSection.Contact, progress.missingSection)
        assertEquals(20, progress.nextPercent)
    }

    @Test
    fun `four completed sections produce eighty percent`() {
        val resume = completeResume().copy(summary = "")

        val progress = resumeProgress(resume)

        assertEquals(80, progress.percent)
        assertEquals(ResumeProgressSection.Summary, progress.missingSection)
        assertEquals(100, progress.nextPercent)
    }

    @Test
    fun `all main sections produce a complete profile`() {
        val progress = resumeProgress(completeResume())

        assertEquals(100, progress.percent)
        assertEquals(null, progress.missingSection)
        assertEquals(100, progress.nextPercent)
    }

    private fun completeResume() = ResumeDocument(
        contact = ResumeContact(
            name = "Paul",
            professionalTitle = "Android Developer",
            email = "paul@example.com",
        ),
        experiences = listOf(ResumeExperience(position = "Developer", company = "Empresa")),
        education = listOf(ResumeEducation(institution = "Universidad", degree = "Ingeniería")),
        skills = listOf("Kotlin"),
        summary = "Desarrollador Android.",
    )
}

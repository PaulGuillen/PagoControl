package com.devpaul.features.resume

import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.ResumeTextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeDraftStateTest {

    @Test
    fun `editing optional sections keeps their existing content`() {
        val course = ResumeCourse(name = "Android", institution = "Google")
        val state = ResumeDraftState()
        state.load(
            ResumeDocument(
                optionalSections = setOf(ResumeOptionalSection.Courses),
                courses = listOf(course),
            ),
        )

        state.updateOptionalSections(setOf(ResumeOptionalSection.Links))

        assertEquals(setOf(ResumeOptionalSection.Links), state.document.optionalSections)
        assertEquals(listOf(course), state.document.courses)
    }

    @Test
    fun `creating a resume clears the previously loaded document`() {
        val state = ResumeDraftState()
        state.load(ResumeDocument(id = "saved", summary = "Existing"))

        state.startNew(setOf(ResumeOptionalSection.Languages))

        assertTrue(state.document.id.isBlank())
        assertTrue(state.document.summary.isBlank())
        assertEquals(setOf(ResumeOptionalSection.Languages), state.document.optionalSections)
    }

    @Test
    fun `summary text and formatting are updated together`() {
        val state = ResumeDraftState()
        val formatting = ResumeTextFormatting(
            spans = listOf(ResumeTextSpan(start = 0, end = 6, bold = true)),
        )

        state.updateSummary("Perfil", formatting)

        assertEquals("Perfil", state.document.summary)
        assertEquals(formatting, state.document.summaryFormatting)
    }

    @Test
    fun `saved snapshot keeps stable entry identifiers`() {
        val course = ResumeCourse(name = "Android")
        val state = ResumeDraftState()
        state.updateCourses(listOf(course))

        val snapshot = state.snapshotForSave()

        assertEquals(course.id, snapshot.courses.single().id)
    }
}

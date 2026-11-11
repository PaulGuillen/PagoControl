package com.devpaul.features.resume

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.resume.ui.contact.ContactScreen
import com.devpaul.features.resume.ui.courses.CoursesScreen
import com.devpaul.features.resume.ui.education.EducationScreen
import com.devpaul.features.resume.ui.experience.ExperienceScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumeDraftEditingTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun experienceSavesMonthYearDates() {
        var savedExperiences = emptyList<ResumeExperience>()
        compose.setContent {
            CvFreeTheme {
                ExperienceScreen(
                    onBack = {},
                    onContinue = {},
                    onExperiencesChange = { savedExperiences = it },
                )
            }
        }

        enterField(0, "Android Developer")
        enterField(1, "Company")
        enterField(3, "07/2025")
        enterField(4, "07/2026")
        saveEntry()

        compose.runOnIdle {
            assertEquals("07/2025", savedExperiences.single().startYear)
            assertEquals("07/2026", savedExperiences.single().endYear)
        }
    }

    @Test
    fun currentExperienceKeepsPresentWithMonthYearStart() {
        var savedExperiences = emptyList<ResumeExperience>()
        compose.setContent {
            CvFreeTheme {
                ExperienceScreen(
                    onBack = {},
                    onContinue = {},
                    onExperiencesChange = { savedExperiences = it },
                )
            }
        }

        enterField(0, "Android Developer")
        enterField(1, "Company")
        enterField(3, "07/2026")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.ui_i_currently_work_here_c5d8e2))
            .performScrollTo()
            .performClick()
        saveEntry()

        compose.runOnIdle {
            val experience = savedExperiences.single()
            assertEquals("07/2026", experience.startYear)
            assertTrue(experience.isOngoing)
            assertTrue(experience.endYear.isEmpty())
        }
    }

    @Test
    fun educationSavesMonthYearDates() {
        var savedEducation = emptyList<ResumeEducation>()
        compose.setContent {
            CvFreeTheme {
                EducationScreen(
                    onBack = {},
                    onContinue = {},
                    onEducationChange = { savedEducation = it },
                )
            }
        }

        enterField(0, "University")
        enterField(1, "Engineering")
        enterField(3, "03/2021")
        enterField(4, "07/2026")
        saveEntry()

        compose.runOnIdle {
            assertEquals("03/2021", savedEducation.single().startYear)
            assertEquals("07/2026", savedEducation.single().endYear)
        }
    }

    @Test
    fun coursesAndCertificationsSaveMonthYearDates() {
        var savedCourses = emptyList<ResumeCourse>()
        compose.setContent {
            CvFreeTheme {
                CoursesScreen(
                    onBack = {},
                    onContinue = {},
                    step = 6,
                    totalSteps = 9,
                    actionText = "Continue",
                    onCoursesChange = { savedCourses = it },
                )
            }
        }

        enterField(0, "Android Certification")
        enterField(2, "01/2026")
        enterField(3, "07/2026")
        saveEntry()

        compose.runOnIdle {
            assertEquals("01/2026", savedCourses.single().startDate)
            assertEquals("07/2026", savedCourses.single().endDate)
        }
    }

    private fun enterField(index: Int, value: String) {
        compose.onAllNodes(hasSetTextAction())[index]
            .performScrollTo()
            .performTextInput(value)
    }

    private fun saveEntry() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.ui_save_a0b626))
            .performScrollTo()
            .performClick()
    }

    @Test
    fun typingAnIncompleteContactUpdatesTheDraftWithoutContinuing() {
        var pendingContact = ResumeContact()
        var continued = false
        compose.setContent {
            CvFreeTheme {
                ContactScreen(
                    onBack = {},
                    onContinue = { continued = true },
                    initialData = ResumeContact(),
                    onDraftChange = { pendingContact = it },
                )
            }
        }

        compose.onAllNodes(hasSetTextAction())[0].performTextInput("Partial name")

        compose.runOnIdle {
            assertEquals("Partial name", pendingContact.name)
            assertTrue(pendingContact.professionalTitle.isEmpty())
            assertFalse(continued)
        }
    }

    @Test
    fun anIncompleteEntryHasAStableIdentifierAndCancelDiscardsIt() {
        var pendingEducation: ResumeEducation? = null
        var confirmed = false
        compose.setContent {
            CvFreeTheme {
                EducationScreen(
                    onBack = {},
                    onContinue = {},
                    onEducationChange = { confirmed = true },
                    onDraftChange = { pendingEducation = it },
                )
            }
        }

        compose.onAllNodes(hasSetTextAction())[0].performTextInput("Draft university")
        var entryId = ""
        compose.runOnIdle {
            val pending = checkNotNull(pendingEducation)
            assertEquals("Draft university", pending.institution)
            assertTrue(pending.degree.isEmpty())
            entryId = pending.id
            assertFalse(entryId.isBlank())
        }
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("Partial degree")
        compose.runOnIdle {
            assertEquals(entryId, pendingEducation?.id)
            assertFalse(confirmed)
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.ui_cancel_c111e0))
            .performScrollTo()
            .performClick()

        compose.runOnIdle {
            assertNull(pendingEducation)
            assertFalse(confirmed)
        }
    }
}

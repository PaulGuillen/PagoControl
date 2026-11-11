package com.devpaul.core.domain.resume

import kotlinx.serialization.Serializable
import java.util.UUID

fun newResumeEntryId(): String = UUID.randomUUID().toString()

@Serializable
enum class ResumeOptionalSection {
    Courses,
    Links,
    Languages,
    References,
}

@Serializable
enum class ResumeTextAlignment {
    Start,
    Center,
    End,
    Justify,
}

@Serializable
data class ResumeTextSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val textColorArgb: Long? = null,
    val backgroundColorArgb: Long? = null,
    val url: String? = null,
)

@Serializable
data class ResumeParagraphStyle(
    val start: Int,
    val end: Int,
    val alignment: ResumeTextAlignment = ResumeTextAlignment.Start,
)

@Serializable
data class ResumeTextFormatting(
    val spans: List<ResumeTextSpan> = emptyList(),
    val paragraphs: List<ResumeParagraphStyle> = emptyList(),
)

@Serializable
data class ResumeContact(
    val name: String = "",
    val lastName: String = "",
    val professionalTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val address: String = "",
)

@Serializable
data class ResumeExperience(
    val position: String = "",
    val company: String = "",
    val city: String = "",
    val startYear: String = "",
    val endYear: String = "",
    val isCurrent: Boolean = false,
    val achievements: String = "",
    val achievementsFormatting: ResumeTextFormatting = ResumeTextFormatting(),
    val id: String = newResumeEntryId(),
) {
    val isOngoing: Boolean
        get() = isCurrent || endYear.trim().lowercase() in currentExperienceLabels

    val displayEndYear: String
        get() = if (isOngoing) "Actualidad" else endYear
}

private val currentExperienceLabels = setOf("actualidad", "actual", "presente", "present", "current")

@Serializable
data class ResumeEducation(
    val institution: String = "",
    val degree: String = "",
    val city: String = "",
    val startYear: String = "",
    val endYear: String = "",
    val id: String = newResumeEntryId(),
)

@Serializable
data class ResumeCourse(
    val name: String = "",
    val institution: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val id: String = newResumeEntryId(),
)

@Serializable
data class ResumeLink(
    val name: String = "",
    val url: String = "",
    val id: String = newResumeEntryId(),
)

@Serializable
data class ResumeLanguage(
    val language: String = "",
    val level: String = "",
    val id: String = newResumeEntryId(),
)

@Serializable
data class ResumeReference(
    val name: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val id: String = newResumeEntryId(),
)

@Serializable
data class ResumeDocument(
    val id: String = "",
    val updatedAt: Long = 0L,
    val templateName: String = "budapest",
    val optionalSections: Set<ResumeOptionalSection> = emptySet(),
    val contact: ResumeContact = ResumeContact(),
    val experiences: List<ResumeExperience> = emptyList(),
    val education: List<ResumeEducation> = emptyList(),
    val skills: List<String> = emptyList(),
    val summary: String = "",
    val summaryFormatting: ResumeTextFormatting = ResumeTextFormatting(),
    val courses: List<ResumeCourse> = emptyList(),
    val links: List<ResumeLink> = emptyList(),
    val languages: List<ResumeLanguage> = emptyList(),
    val references: List<ResumeReference> = emptyList(),
) {
    val displayName: String
        get() = contact.professionalTitle.ifBlank {
            listOf(contact.name, contact.lastName)
                .filter(String::isNotBlank)
                .joinToString(" ")
                .ifBlank { "Mi currículum" }
        }
}

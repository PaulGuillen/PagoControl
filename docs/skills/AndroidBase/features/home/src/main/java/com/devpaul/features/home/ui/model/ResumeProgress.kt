package com.devpaul.features.home.ui.model

import com.devpaul.core.domain.resume.ResumeDocument

internal data class ResumeProgress(
    val percent: Int,
    val missingSection: ResumeProgressSection?,
    val nextPercent: Int,
)

internal enum class ResumeProgressSection {
    Contact,
    Experience,
    Education,
    Skills,
    Summary,
}

internal fun resumeProgress(resume: ResumeDocument): ResumeProgress {
    val sections = listOf(
        ResumeProgressSection.Contact to (
            resume.contact.name.isNotBlank() &&
                resume.contact.professionalTitle.isNotBlank() &&
                (resume.contact.email.isNotBlank() || resume.contact.phone.isNotBlank())
            ),
        ResumeProgressSection.Experience to resume.experiences.any {
            it.position.isNotBlank() && it.company.isNotBlank()
        },
        ResumeProgressSection.Education to resume.education.any {
            it.institution.isNotBlank() && it.degree.isNotBlank()
        },
        ResumeProgressSection.Skills to resume.skills.any(String::isNotBlank),
        ResumeProgressSection.Summary to resume.summary.isNotBlank(),
    )
    val completed = sections.count { it.second }
    val percent = completed * 100 / sections.size
    val missingSection = sections.firstOrNull { !it.second }?.first
    return ResumeProgress(
        percent = percent,
        missingSection = missingSection,
        nextPercent = ((completed + 1).coerceAtMost(sections.size) * 100 / sections.size),
    )
}

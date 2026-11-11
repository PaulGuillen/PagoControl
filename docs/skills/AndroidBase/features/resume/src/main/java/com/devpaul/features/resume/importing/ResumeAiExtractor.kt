package com.devpaul.features.resume.importing

import android.content.Context

import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeDocumentCodec
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeLanguage
import com.devpaul.core.domain.resume.ResumeLink
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeReference
import com.google.firebase.functions.FirebaseFunctions
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object ResumeAiExtractor {
    private const val MAX_SOURCE_LENGTH = 60_000

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun enhance(
        context: Context,
        content: ResumePdfTextContent,
        localDocument: ResumeDocument,
    ): ResumeDocument? {
        val source = content.forAi().take(MAX_SOURCE_LENGTH)
        if (source.length < 80) return null

        val result = FirebaseFunctions.getInstance("us-central1")
            .getHttpsCallable("enhanceResume")
            .apply { setTimeout(70, TimeUnit.SECONDS) }
            .call(
                mapOf(
                    "deviceHash" to resumeAiDeviceHash(context),
                    "source" to source,
                    "localDraft" to ResumeDocumentCodec.encode(localDocument),
                ),
            ).await().data as? Map<*, *> ?: error("Invalid AI response")
        when (result["status"]) {
            "limited" -> throw ResumeAiQuotaException(
                (result["nextAvailableAtMs"] as? Number)?.toLong(),
            )
            "busy" -> throw ResumeAiQuotaException(null)
            "enhanced" -> Unit
            else -> error("Invalid AI status")
        }
        val response = result["resumeJson"] as? String ?: error("Missing AI resume")
        val extracted = json.decodeFromString<AiResume>(response)
        return extracted.mergeWith(localDocument)
    }

    private fun ResumePdfTextContent.forAi(): String = buildString {
        if (headerText.isNotBlank()) {
            appendLine("[HEADER]")
            appendLine(headerText)
        }
        if (sidebarText.isNotBlank()) {
            appendLine("[SIDEBAR]")
            appendLine(sidebarText)
        }
        if (mainText.isNotBlank()) {
            appendLine("[MAIN CONTENT]")
            appendLine(mainText)
        }
        if (layoutText.isNotBlank()) {
            appendLine("[PAGE LAYOUT: x/y are percentages]")
            appendLine(layoutText)
        }
        appendLine("[FULL TEXT]")
        appendLine(fullText)
    }
}

@Serializable
private data class AiResume(
    val contact: AiContact = AiContact(),
    val experiences: List<AiExperience> = emptyList(),
    val education: List<AiEducation> = emptyList(),
    val skills: List<String> = emptyList(),
    val summary: String = "",
    val courses: List<AiCourse> = emptyList(),
    val links: List<AiLink> = emptyList(),
    val languages: List<AiLanguage> = emptyList(),
    val references: List<AiReference> = emptyList(),
)

@Serializable
private data class AiContact(
    val name: String = "",
    val lastName: String = "",
    val professionalTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val address: String = "",
)

@Serializable
private data class AiExperience(
    val position: String = "",
    val company: String = "",
    val city: String = "",
    val startYear: String = "",
    val endYear: String = "",
    val achievements: String = "",
)

@Serializable
private data class AiEducation(
    val institution: String = "",
    val degree: String = "",
    val city: String = "",
    val startYear: String = "",
    val endYear: String = "",
)

@Serializable
private data class AiCourse(
    val name: String = "",
    val institution: String = "",
    val startDate: String = "",
    val endDate: String = "",
)

@Serializable
private data class AiLink(val name: String = "", val url: String = "")

@Serializable
private data class AiLanguage(val language: String = "", val level: String = "")

@Serializable
private data class AiReference(
    val name: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
)

private fun AiResume.mergeWith(local: ResumeDocument): ResumeDocument {
    val aiExperiences = experiences.mapNotNull { item ->
        item.takeIf { it.position.isNotBlank() || it.company.isNotBlank() }?.let {
            ResumeExperience(
                position = it.position.clean(),
                company = it.company.clean(),
                city = it.city.clean(),
                startYear = it.startYear.clean(),
                endYear = it.endYear.clean(),
                isCurrent = it.endYear.clean().lowercase() in setOf("actual", "actualidad", "presente", "present", "current"),
                achievements = it.achievements.cleanMultiline(),
            )
        }
    }
    val aiEducation = education.mapNotNull { item ->
        item.takeIf { it.institution.isNotBlank() || it.degree.isNotBlank() }?.let {
            ResumeEducation(
                institution = it.institution.clean(),
                degree = it.degree.clean(),
                city = it.city.clean(),
                startYear = it.startYear.clean(),
                endYear = it.endYear.clean(),
            )
        }
    }
    val aiCourses = courses.mapNotNull { item ->
        item.takeIf { it.name.isNotBlank() }?.let {
            ResumeCourse(
                name = it.name.clean(),
                institution = it.institution.clean(),
                startDate = it.startDate.clean(),
                endDate = it.endDate.clean(),
            )
        }
    }
    val aiLinks = links.mapNotNull { item ->
        item.takeIf { it.url.isNotBlank() }?.let { ResumeLink(it.name.clean(), it.url.clean()) }
    }
    val aiLanguages = languages.mapNotNull { item ->
        item.takeIf { it.language.isNotBlank() }?.let { ResumeLanguage(it.language.clean(), it.level.clean()) }
    }
    val aiReferences = references.mapNotNull { item ->
        item.takeIf { it.name.isNotBlank() }?.let {
            ResumeReference(it.name.clean(), it.company.clean(), it.phone.clean(), it.email.clean())
        }
    }

    val mergedExperiences = aiExperiences.preferNonLossy(local.experiences) {
        listOf(it.position, it.company, it.city, it.startYear, it.endYear, it.achievements).filledCount()
    }
    val mergedEducation = aiEducation.preferNonLossy(local.education) {
        listOf(it.institution, it.degree, it.city, it.startYear, it.endYear).filledCount()
    }
    val mergedCourses = aiCourses.preferNonLossy(local.courses) {
        listOf(it.name, it.institution, it.startDate, it.endDate).filledCount()
    }
    val mergedLinks = aiLinks.preferNonLossy(local.links) {
        listOf(it.name, it.url).filledCount()
    }
    val mergedLanguages = aiLanguages.preferNonLossy(local.languages) {
        listOf(it.language, it.level).filledCount()
    }
    val mergedReferences = aiReferences.preferNonLossy(local.references) {
        listOf(it.name, it.company, it.phone, it.email).filledCount()
    }
    return local.copy(
        contact = ResumeContact(
            name = contact.name.clean().ifBlank { local.contact.name },
            lastName = contact.lastName.clean().ifBlank { local.contact.lastName },
            professionalTitle = contact.professionalTitle.clean().ifBlank { local.contact.professionalTitle },
            email = contact.email.clean().ifBlank { local.contact.email },
            phone = contact.phone.clean().ifBlank { local.contact.phone },
            city = contact.city.clean().ifBlank { local.contact.city },
            address = contact.address.clean().ifBlank { local.contact.address },
        ),
        experiences = mergedExperiences,
        education = mergedEducation,
        skills = ResumeTextParser.normalizeImportedSkills(skills.map(String::clean))
            .preferComplete(local.skills),
        summary = summary.cleanMultiline().ifBlank { local.summary },
        courses = mergedCourses,
        links = mergedLinks,
        languages = mergedLanguages,
        references = mergedReferences,
        optionalSections = buildSet {
            if (mergedCourses.isNotEmpty()) add(ResumeOptionalSection.Courses)
            if (mergedLinks.isNotEmpty()) add(ResumeOptionalSection.Links)
            if (mergedLanguages.isNotEmpty()) add(ResumeOptionalSection.Languages)
            if (mergedReferences.isNotEmpty()) add(ResumeOptionalSection.References)
        },
    )
}

private fun <T> List<T>.preferComplete(fallback: List<T>): List<T> =
    if (size >= fallback.size && isNotEmpty()) this else fallback

private fun <T> List<T>.preferNonLossy(
    fallback: List<T>,
    completeness: (T) -> Int,
): List<T> = if (
    isNotEmpty() &&
    size >= fallback.size &&
    sumOf(completeness) >= fallback.sumOf(completeness)
) {
    this
} else {
    fallback
}

private fun List<String>.filledCount(): Int = count(String::isNotBlank)

private fun String.clean(): String = trim().replace(Regex("[ \\t]+"), " ")

private fun String.cleanMultiline(): String = lineSequence()
    .map(String::clean)
    .filter(String::isNotBlank)
    .joinToString("\n")

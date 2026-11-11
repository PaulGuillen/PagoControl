package com.devpaul.core.data.resume

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeLanguage
import com.devpaul.core.domain.resume.ResumeLink
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeReference
import com.devpaul.core.domain.resume.ResumeParagraphStyle
import com.devpaul.core.domain.resume.ResumeTextAlignment
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.ResumeTextSpan
import com.devpaul.core.domain.resume.newResumeEntryId
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.resumeDataStore by preferencesDataStore(name = "saved_resumes")
private val resumesKey = stringPreferencesKey("resumes_json")
private val migrationOwnerKey = stringPreferencesKey("migration_owner_uid")

class LegacyResumeStore(
    private val context: Context,
) : LegacyResumeSource {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun read(): List<ResumeDocument> =
        decode(context.resumeDataStore.data.first()[resumesKey])

    override suspend fun ownerUid(): String? = context.resumeDataStore.data.first()[migrationOwnerKey]

    override suspend fun claim(uid: String) {
        context.resumeDataStore.edit { preferences ->
            val owner = preferences[migrationOwnerKey]
            check(owner == null || owner == uid) { "Local resumes belong to another migration owner" }
            preferences[migrationOwnerKey] = uid
        }
    }

    override suspend fun remove(resumeId: String) {
        context.resumeDataStore.edit { preferences ->
            val updated = decode(preferences[resumesKey]).filterNot { it.id == resumeId }
            preferences[resumesKey] = json.encodeToString(updated.map(StoredResume::fromDomain))
        }
    }

    private fun decode(value: String?): List<ResumeDocument> =
        if (value.isNullOrBlank()) {
            emptyList()
        } else {
            json.decodeFromString<List<StoredResume>>(value).map(StoredResume::toDomain)
        }
}

@Serializable
private data class StoredResume(
    val id: String,
    val updatedAt: Long,
    val templateName: String,
    val optionalSections: Set<String>,
    val contact: StoredContact,
    val experiences: List<StoredExperience>,
    val education: List<StoredEducation>,
    val skills: List<String>,
    val summary: String,
    val summaryFormatting: StoredTextFormatting = StoredTextFormatting(),
    val courses: List<StoredCourse>,
    val links: List<StoredLink>,
    val languages: List<StoredLanguage>,
    val references: List<StoredReference>,
) {
    fun toDomain() = ResumeDocument(
        id = id,
        updatedAt = updatedAt,
        templateName = templateName,
        optionalSections = optionalSections.mapNotNull { name ->
            ResumeOptionalSection.entries.firstOrNull { it.name == name }
        }.toSet(),
        contact = contact.toDomain(),
        experiences = experiences.map(StoredExperience::toDomain),
        education = education.map(StoredEducation::toDomain),
        skills = skills,
        summary = summary,
        summaryFormatting = summaryFormatting.toDomain(),
        courses = courses.map(StoredCourse::toDomain),
        links = links.map(StoredLink::toDomain),
        languages = languages.map(StoredLanguage::toDomain),
        references = references.map(StoredReference::toDomain),
    )

    companion object {
        fun fromDomain(value: ResumeDocument) = StoredResume(
            id = value.id,
            updatedAt = value.updatedAt,
            templateName = value.templateName,
            optionalSections = value.optionalSections.map(ResumeOptionalSection::name).toSet(),
            contact = StoredContact.fromDomain(value.contact),
            experiences = value.experiences.map(StoredExperience::fromDomain),
            education = value.education.map(StoredEducation::fromDomain),
            skills = value.skills,
            summary = value.summary,
            summaryFormatting = StoredTextFormatting.fromDomain(value.summaryFormatting),
            courses = value.courses.map(StoredCourse::fromDomain),
            links = value.links.map(StoredLink::fromDomain),
            languages = value.languages.map(StoredLanguage::fromDomain),
            references = value.references.map(StoredReference::fromDomain),
        )
    }
}

@Serializable
private data class StoredTextFormatting(
    val spans: List<StoredTextSpan> = emptyList(),
    val paragraphs: List<StoredParagraphStyle> = emptyList(),
) {
    fun toDomain() = ResumeTextFormatting(
        spans = spans.map(StoredTextSpan::toDomain),
        paragraphs = paragraphs.map(StoredParagraphStyle::toDomain),
    )

    companion object {
        fun fromDomain(value: ResumeTextFormatting) = StoredTextFormatting(
            spans = value.spans.map(StoredTextSpan::fromDomain),
            paragraphs = value.paragraphs.map(StoredParagraphStyle::fromDomain),
        )
    }
}

@Serializable
private data class StoredTextSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val textColorArgb: Long? = null,
    val backgroundColorArgb: Long? = null,
    val url: String? = null,
) {
    fun toDomain() = ResumeTextSpan(
        start, end, bold, italic, underline, strikethrough,
        textColorArgb, backgroundColorArgb, url,
    )

    companion object {
        fun fromDomain(value: ResumeTextSpan) = StoredTextSpan(
            value.start, value.end, value.bold, value.italic, value.underline,
            value.strikethrough, value.textColorArgb, value.backgroundColorArgb, value.url,
        )
    }
}

@Serializable
private data class StoredParagraphStyle(
    val start: Int,
    val end: Int,
    val alignment: String = ResumeTextAlignment.Start.name,
) {
    fun toDomain() = ResumeParagraphStyle(
        start = start,
        end = end,
        alignment = ResumeTextAlignment.entries.firstOrNull { it.name == alignment }
            ?: ResumeTextAlignment.Start,
    )

    companion object {
        fun fromDomain(value: ResumeParagraphStyle) = StoredParagraphStyle(
            value.start,
            value.end,
            value.alignment.name,
        )
    }
}

@Serializable
private data class StoredContact(
    val name: String,
    val lastName: String,
    val professionalTitle: String,
    val email: String,
    val phone: String,
    val city: String,
    val address: String,
) {
    fun toDomain() = ResumeContact(name, lastName, professionalTitle, email, phone, city, address)

    companion object {
        fun fromDomain(value: ResumeContact) = StoredContact(
            value.name,
            value.lastName,
            value.professionalTitle,
            value.email,
            value.phone,
            value.city,
            value.address,
        )
    }
}

@Serializable
private data class StoredExperience(
    val position: String,
    val company: String,
    val city: String,
    val startYear: String,
    val endYear: String,
    val isCurrent: Boolean = false,
    val achievements: String,
    val achievementsFormatting: StoredTextFormatting = StoredTextFormatting(),
    val id: String = "",
) {
    fun toDomain() = ResumeExperience(
        position = position,
        company = company,
        city = city,
        startYear = startYear,
        endYear = endYear,
        isCurrent = isCurrent,
        achievements = achievements,
        achievementsFormatting = achievementsFormatting.toDomain(),
        id = id.ifBlank(::newResumeEntryId),
    )

    companion object {
        fun fromDomain(value: ResumeExperience) = StoredExperience(
            position = value.position,
            company = value.company,
            city = value.city,
            startYear = value.startYear,
            endYear = value.endYear,
            isCurrent = value.isOngoing,
            achievements = value.achievements,
            achievementsFormatting = StoredTextFormatting.fromDomain(value.achievementsFormatting),
            id = value.id,
        )
    }
}

@Serializable
private data class StoredEducation(
    val institution: String,
    val degree: String,
    val city: String,
    val startYear: String,
    val endYear: String,
    val id: String = "",
) {
    fun toDomain() = ResumeEducation(institution, degree, city, startYear, endYear, id.ifBlank(::newResumeEntryId))

    companion object {
        fun fromDomain(value: ResumeEducation) = StoredEducation(
            value.institution,
            value.degree,
            value.city,
            value.startYear,
            value.endYear,
            value.id,
        )
    }
}

@Serializable
private data class StoredCourse(
    val name: String,
    val institution: String,
    val startDate: String,
    val endDate: String,
    val id: String = "",
) {
    fun toDomain() = ResumeCourse(name, institution, startDate, endDate, id.ifBlank(::newResumeEntryId))

    companion object {
        fun fromDomain(value: ResumeCourse) = StoredCourse(
            value.name,
            value.institution,
            value.startDate,
            value.endDate,
            value.id,
        )
    }
}

@Serializable
private data class StoredLink(val name: String, val url: String, val id: String = "") {
    fun toDomain() = ResumeLink(name, url, id.ifBlank(::newResumeEntryId))

    companion object {
        fun fromDomain(value: ResumeLink) = StoredLink(value.name, value.url, value.id)
    }
}

@Serializable
private data class StoredLanguage(val language: String, val level: String, val id: String = "") {
    fun toDomain() = ResumeLanguage(language, level, id.ifBlank(::newResumeEntryId))

    companion object {
        fun fromDomain(value: ResumeLanguage) = StoredLanguage(value.language, value.level, value.id)
    }
}

@Serializable
private data class StoredReference(
    val name: String,
    val company: String,
    val phone: String,
    val email: String,
    val id: String = "",
) {
    fun toDomain() = ResumeReference(name, company, phone, email, id.ifBlank(::newResumeEntryId))

    companion object {
        fun fromDomain(value: ResumeReference) = StoredReference(
            value.name,
            value.company,
            value.phone,
            value.email,
            value.id,
        )
    }
}

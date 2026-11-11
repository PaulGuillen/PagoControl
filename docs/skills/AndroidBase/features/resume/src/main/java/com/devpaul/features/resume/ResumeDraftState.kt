package com.devpaul.features.resume

import androidx.compose.runtime.Stable
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeCourse
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeLanguage
import com.devpaul.core.domain.resume.ResumeLink
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeReference
import com.devpaul.core.domain.resume.ResumeRepository
import com.devpaul.core.domain.resume.ResumeTextFormatting
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Stable
class ResumeDraftState(private val savedStateHandle: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    var document by mutableStateOf(
        savedStateHandle.get<String>(DRAFT_KEY)?.let { saved ->
            runCatching { Json.decodeFromString<ResumeDocument>(saved) }
                .onFailure { AppAnalytics.error("restore_resume_draft", it) }.getOrNull()
        } ?: defaultResume(),
    )
        private set

    var autoSaveErrorCount by mutableStateOf(0)
        private set
    private var autoSaver: ResumeAutoSaver? = null
    private var pendingEdits by mutableStateOf<Map<ResumeDraftSection, ResumeDocument>>(emptyMap())
    private var hasChanges = savedStateHandle.get<Boolean>(CHANGES_KEY) ?: false
    var sessionId = 0L
        private set

    val editingDocument: ResumeDocument
        get() = pendingEdits.entries.fold(document) { current, (section, pending) ->
            mergeSection(current, pending, section)
        }

    fun configureAutoSave(repository: ResumeRepository, enabled: Boolean) {
        val ownerId = repository.currentOwnerId
        val previousOwner = savedStateHandle.get<String>(OWNER_KEY)
        if (ownerId != previousOwner) {
            autoSaver?.enabled = false
            reset(defaultResume())
            savedStateHandle[OWNER_KEY] = ownerId
        }
        val isFirstConfiguration = autoSaver == null
        val saver = autoSaver ?: ResumeAutoSaver(repository, viewModelScope, onAutoSaved = ::onAutoSaved) {
            autoSaveErrorCount++
        }.also { autoSaver = it }
        val wasEnabled = saver.enabled
        saver.enabled = enabled
        if (enabled && (isFirstConfiguration || !wasEnabled) && hasChanges) scheduleAutoSave()
    }

    fun flushAutoSave() {
        autoSaver?.flush()
    }

    fun updatePending(section: ResumeDraftSection, pending: ResumeDocument?) {
        val previous = editingDocument
        pendingEdits = if (pending == null) pendingEdits - section else pendingEdits + (section to pending)
        if (editingDocument != previous) changed()
    }

    fun commitPending(section: ResumeDraftSection, session: Long = sessionId) {
        if (session != sessionId) return
        val pending = pendingEdits[section] ?: return
        document = mergeSection(document, pending, section)
        pendingEdits = pendingEdits - section
    }

    suspend fun save(): ResumeDocument {
        val snapshot = snapshotForSave()
        checkNotNull(autoSaver).save(snapshot)
        AppAnalytics.outcome("save_resume", "success")
        document = editingDocument.copy(id = snapshot.id, updatedAt = snapshot.updatedAt)
        pendingEdits = emptyMap()
        hasChanges = false
        rememberDraft()
        return snapshot
    }

    suspend fun delete(resumeId: String) {
        if (document.id == resumeId) reset(defaultResume())
        checkNotNull(autoSaver).delete(resumeId)
    }

    private fun reset(resume: ResumeDocument) {
        document = resume
        pendingEdits = emptyMap()
        hasChanges = false
        sessionId++
        rememberDraft()
    }

    private fun changed() {
        hasChanges = true
        scheduleAutoSave()
        rememberDraft()
    }

    private fun onAutoSaved(saved: ResumeDocument) {
        if (document.id != saved.id) return
        if (editingDocument.copy(updatedAt = 0) == saved.copy(updatedAt = 0)) hasChanges = false
        document = document.copy(updatedAt = saved.updatedAt)
        rememberDraft()
    }

    private fun rememberDraft() {
        savedStateHandle[DRAFT_KEY] = Json.encodeToString(editingDocument)
        savedStateHandle[CHANGES_KEY] = hasChanges
    }

    private fun scheduleAutoSave() {
        val saver = autoSaver ?: return
        if (!saver.enabled) return
        if (document.id.isBlank()) document = document.copy(id = UUID.randomUUID().toString())
        saver.schedule(snapshotForSave())
    }

    private fun update(section: ResumeDraftSection, value: ResumeDocument) {
        val previous = editingDocument
        pendingEdits = pendingEdits - section
        document = value
        if (editingDocument != previous) changed()
    }

    fun startNew(optionalSections: Set<ResumeOptionalSection>) {
        flushAutoSave()
        reset(defaultResume().copy(optionalSections = optionalSections))
    }

    fun importResume(resume: ResumeDocument) {
        flushAutoSave()
        reset(resume.copy(id = "", updatedAt = 0L))
        changed()
        flushAutoSave()
    }

    fun load(resume: ResumeDocument) {
        flushAutoSave()
        if (resume.id.isNotBlank() && resume.id == document.id &&
            (hasChanges || pendingEdits.isNotEmpty() || document.updatedAt > resume.updatedAt)
        ) {
            document = editingDocument
            pendingEdits = emptyMap()
            sessionId++
            rememberDraft()
            return
        }
        reset(resume)
    }

    fun updateContact(value: ResumeContact) {
        update(ResumeDraftSection.Contact, document.copy(contact = value))
    }

    fun updateExperiences(value: List<ResumeExperience>) {
        update(ResumeDraftSection.Experience, document.copy(experiences = value))
    }

    fun updateEducation(value: List<ResumeEducation>) {
        update(ResumeDraftSection.Education, document.copy(education = value))
    }

    fun updateSkills(value: List<String>) {
        update(ResumeDraftSection.Skills, document.copy(skills = value))
    }

    fun updateOptionalSections(value: Set<ResumeOptionalSection>) {
        update(ResumeDraftSection.Sections, document.copy(optionalSections = value))
    }

    fun updateSummary(value: String, formatting: ResumeTextFormatting = document.summaryFormatting) {
        update(ResumeDraftSection.Summary, document.copy(summary = value, summaryFormatting = formatting))
    }

    fun updateCourses(value: List<ResumeCourse>) {
        update(ResumeDraftSection.Courses, document.copy(courses = value))
    }

    fun updateLinks(value: List<ResumeLink>) {
        update(ResumeDraftSection.Links, document.copy(links = value))
    }

    fun updateLanguages(value: List<ResumeLanguage>) {
        update(ResumeDraftSection.Languages, document.copy(languages = value))
    }

    fun updateReferences(value: List<ResumeReference>) {
        update(ResumeDraftSection.References, document.copy(references = value))
    }

    fun updateTemplate(value: String) {
        update(ResumeDraftSection.Template, document.copy(templateName = value))
    }

    fun snapshotForSave(): ResumeDocument = editingDocument.copy(
        id = document.id.ifBlank { UUID.randomUUID().toString() },
        updatedAt = System.currentTimeMillis(),
    )

    private fun mergeSection(
        current: ResumeDocument,
        pending: ResumeDocument,
        section: ResumeDraftSection,
    ): ResumeDocument = when (section) {
        ResumeDraftSection.Contact -> current.copy(contact = pending.contact)
        ResumeDraftSection.Experience -> current.copy(experiences = pending.experiences)
        ResumeDraftSection.Education -> current.copy(education = pending.education)
        ResumeDraftSection.Skills -> current.copy(skills = pending.skills)
        ResumeDraftSection.Summary -> current.copy(summary = pending.summary, summaryFormatting = pending.summaryFormatting)
        ResumeDraftSection.Courses -> current.copy(courses = pending.courses)
        ResumeDraftSection.Links -> current.copy(links = pending.links)
        ResumeDraftSection.Languages -> current.copy(languages = pending.languages)
        ResumeDraftSection.References -> current.copy(references = pending.references)
        ResumeDraftSection.Sections -> current.copy(optionalSections = pending.optionalSections)
        ResumeDraftSection.Template -> current.copy(templateName = pending.templateName)
    }

    private companion object {
        const val OWNER_KEY = "resume_draft_owner"
        const val DRAFT_KEY = "active_resume_draft"
        const val CHANGES_KEY = "active_resume_has_changes"
        fun defaultResume() = ResumeDocument()
    }
}

enum class ResumeDraftSection {
    Contact,
    Experience,
    Education,
    Skills,
    Summary,
    Courses,
    Links,
    Languages,
    References,
    Sections,
    Template,
}

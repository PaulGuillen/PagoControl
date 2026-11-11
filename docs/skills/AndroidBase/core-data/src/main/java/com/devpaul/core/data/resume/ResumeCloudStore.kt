package com.devpaul.core.data.resume

import com.devpaul.core.domain.resume.ResumeDocument
import kotlinx.coroutines.flow.Flow

internal interface ResumeCloudStore {
    val currentUid: String?
    val userIds: Flow<String?>
    fun observe(uid: String): Flow<ResumeCloudSnapshot>
    fun save(uid: String, resume: ResumeDocument, onFailure: (Exception) -> Unit)
    fun delete(uid: String, resumeId: String, onFailure: (Exception) -> Unit)
    suspend fun importIfAbsent(uid: String, resume: ResumeDocument)
}

internal data class ResumeCloudSnapshot(
    val resumes: List<ResumeDocument>,
    val hasPendingWrites: Boolean = false,
    val isFromCache: Boolean = false,
)

interface LegacyResumeSource {
    suspend fun read(): List<ResumeDocument>
    suspend fun ownerUid(): String?
    suspend fun claim(uid: String)
    suspend fun remove(resumeId: String)
}

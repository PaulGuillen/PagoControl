package com.devpaul.core.domain.resume

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class ResumeStorageState(
    val isLoading: Boolean = false,
    val hasPendingWrites: Boolean = false,
    val hasError: Boolean = false,
    val hasLocalResumes: Boolean = false,
    val isMigrating: Boolean = false,
    val isFromCache: Boolean = false,
)

interface ResumeRepository {
    val currentOwnerId: String?
        get() = null

    val resumes: Flow<List<ResumeDocument>>

    val storageState: Flow<ResumeStorageState>
        get() = flowOf(ResumeStorageState())

    suspend fun migrateLocalResumes() = Unit

    fun retrySync() = Unit

    suspend fun save(resume: ResumeDocument)

    suspend fun delete(resumeId: String)
}

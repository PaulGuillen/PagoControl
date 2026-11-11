package com.devpaul.core.data.resume

import android.content.Context
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeRepository
import com.devpaul.core.domain.resume.ResumeStorageState
import com.devpaul.core.platform.analytics.AppAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single([ResumeRepository::class])
class FirestoreResumeRepository(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    context: Context,
) : ResumeRepository by CloudResumeRepository(
    FirebaseResumeCloudStore(auth, firestore),
    LegacyResumeStore(context),
    CoroutineScope(SupervisorJob() + Dispatchers.IO),
)

internal class CloudResumeRepository(
    private val cloud: ResumeCloudStore,
    private val legacy: LegacyResumeSource,
    scope: CoroutineScope,
) : ResumeRepository {
    private val documents = MutableStateFlow<List<ResumeDocument>>(emptyList())
    private val state = MutableStateFlow(ResumeStorageState(isLoading = true))
    private val retries = MutableStateFlow(0L)
    private val migrationMutex = Mutex()

    override val resumes = documents.asStateFlow()
    override val storageState = state.asStateFlow()
    override val currentOwnerId: String?
        get() = cloud.currentUid

    init {
        scope.launch {
            combine(cloud.userIds, retries) { uid, _ -> uid }.collectLatest { uid ->
                documents.value = emptyList()
                state.value = ResumeStorageState(isLoading = uid != null)
                if (uid == null) return@collectLatest
                try {
                    val owner = legacy.ownerUid()
                    val hasLocal = (owner == null || owner == uid) && legacy.read().isNotEmpty()
                    if (cloud.currentUid != uid) return@collectLatest
                    state.update { it.copy(hasLocalResumes = hasLocal) }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    reportError(uid, "read_legacy_resumes", error)
                }
                cloud.observe(uid).catch { error ->
                    reportError(uid, "load_cloud_resumes", error)
                }.collect { snapshot ->
                    if (cloud.currentUid == uid) {
                        documents.value = snapshot.resumes.sortedByDescending(ResumeDocument::updatedAt)
                        state.update {
                            it.copy(
                                isLoading = false,
                                hasPendingWrites = snapshot.hasPendingWrites,
                                isFromCache = snapshot.isFromCache,
                            )
                        }
                    }
                }
            }
        }
    }

    override suspend fun save(resume: ResumeDocument) {
        val uid = requireUser()
        FirestoreResumeCodec.encode(resume)
        state.update { it.copy(hasPendingWrites = true) }
        cloud.save(uid, resume) { reportError(uid, "save_cloud_resume", it) }
    }

    override suspend fun delete(resumeId: String) {
        val uid = requireUser()
        FirestoreResumeCodec.requireValidId(resumeId)
        state.update { it.copy(hasPendingWrites = true) }
        cloud.delete(uid, resumeId) { reportError(uid, "delete_cloud_resume", it) }
    }

    override fun retrySync() {
        retries.update { it + 1 }
    }

    override suspend fun migrateLocalResumes() = migrationMutex.withLock {
        val uid = requireUser()
        state.update { it.copy(isMigrating = true, hasError = false) }
        try {
            legacy.claim(uid)
            val localResumes = legacy.read()
            for (resume in localResumes) {
                check(requireUser() == uid) { "Account changed during migration" }
                val migrated = resume.copy(id = legacyDocumentId(resume.id))
                cloud.importIfAbsent(uid, migrated)
                legacy.remove(resume.id)
            }
            if (cloud.currentUid == uid) state.update { it.copy(hasLocalResumes = false) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            reportError(uid, "migrate_local_resumes", error)
            throw error
        } finally {
            if (cloud.currentUid == uid) state.update { it.copy(isMigrating = false) }
        }
    }

    private fun requireUser(): String = checkNotNull(cloud.currentUid) { "No authenticated user" }

    private fun reportError(uid: String, action: String, error: Throwable) {
        AppAnalytics.error(action, error)
        if (cloud.currentUid == uid) state.update { it.copy(isLoading = false, hasError = true) }
    }
}

internal fun legacyDocumentId(originalId: String): String {
    require(originalId.isNotBlank()) { "Missing legacy resume identifier" }
    val digest = MessageDigest.getInstance("SHA-256").digest(originalId.toByteArray(Charsets.UTF_8))
    return "legacy_" + digest.joinToString("") { "%02x".format(it) }
}

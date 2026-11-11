package com.devpaul.features.resume

import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.domain.resume.ResumeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ResumeAutoSaver(
    private val repository: ResumeRepository,
    private val scope: CoroutineScope,
    private val onAutoSaved: (ResumeDocument) -> Unit = {},
    private val onAutoSaveError: () -> Unit,
) {
    var enabled = true
        set(value) {
            field = value
            if (!value) {
                pendingJob?.cancel()
                pendingDocument = null
            }
        }

    private val operations = Channel<Operation>(Channel.UNLIMITED)
    private var pendingJob: Job? = null
    private var pendingDocument: ResumeDocument? = null
    private var pendingOwnerId: String? = null

    init {
        scope.launch {
            for (operation in operations) {
                try {
                    check(operation.ownerId == repository.currentOwnerId) { "Resume account changed" }
                    when (operation) {
                        is Save -> {
                            if (operation.completion != null || enabled) {
                                repository.save(operation.document)
                                if (operation.completion == null) onAutoSaved(operation.document)
                            }
                            operation.completion?.complete(Unit)
                        }
                        is Delete -> {
                            repository.delete(operation.resumeId)
                            AppAnalytics.outcome("delete_resume", "success")
                            operation.completion.complete(Unit)
                        }
                    }
                } catch (error: CancellationException) {
                    operation.completion?.cancel(error)
                    throw error
                } catch (error: Exception) {
                    val action = when (operation) {
                        is Save -> if (operation.completion == null) "auto_save_resume" else "save_resume"
                        is Delete -> "delete_resume"
                    }
                    AppAnalytics.error(action, error)
                    operation.completion?.completeExceptionally(error) ?: onAutoSaveError()
                }
            }
        }
    }

    fun schedule(document: ResumeDocument) {
        if (!enabled) return
        pendingJob?.cancel()
        pendingDocument = document
        pendingOwnerId = repository.currentOwnerId
        pendingJob = scope.launch {
            delay(500)
            flush()
        }
    }

    fun flush() {
        val document = pendingDocument ?: return
        pendingDocument = null
        if (enabled) operations.trySend(Save(document, ownerId = pendingOwnerId))
    }

    suspend fun save(document: ResumeDocument) {
        pendingJob?.cancel()
        pendingDocument = null
        val completion = CompletableDeferred<Unit>()
        operations.send(Save(document, completion, repository.currentOwnerId))
        completion.await()
    }

    suspend fun delete(resumeId: String) {
        if (pendingDocument?.id == resumeId) {
            pendingJob?.cancel()
            pendingDocument = null
        }
        val completion = CompletableDeferred<Unit>()
        operations.send(Delete(resumeId, completion, repository.currentOwnerId))
        completion.await()
    }

    private sealed interface Operation {
        val completion: CompletableDeferred<Unit>?
        val ownerId: String?
    }

    private data class Save(
        val document: ResumeDocument,
        override val completion: CompletableDeferred<Unit>? = null,
        override val ownerId: String? = null,
    ) : Operation

    private data class Delete(
        val resumeId: String,
        override val completion: CompletableDeferred<Unit>,
        override val ownerId: String?,
    ) : Operation
}

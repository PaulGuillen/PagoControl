package com.devpaul.features.resume

import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResumeAutoSaverTest {
    @Test
    fun `switching accounts prevents a pending draft from being written to the new account`() = runTest {
        val repository = TestResumeRepository().apply { currentOwnerId = "first" }
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "private", summary = "First account"))
        repository.currentOwnerId = "second"
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun `switching accounts rejects saves already waiting in the operation queue`() = runTest {
        val repository = TestResumeRepository().apply { currentOwnerId = "first" }
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "private"))
        saver.flush()
        repository.currentOwnerId = "second"
        runCurrent()
        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun `rapid edits save only the latest draft after a pause`() = runTest {
        val repository = TestResumeRepository()
        val saver = ResumeAutoSaver(repository, backgroundScope) { error("Unexpected save failure") }

        saver.schedule(ResumeDocument(id = "draft", summary = "First"))
        advanceTimeBy(300)
        saver.schedule(ResumeDocument(id = "draft", summary = "Latest"))
        advanceTimeBy(499)
        runCurrent()
        assertTrue(repository.writes.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("Latest"), repository.writes.map { it.summary })
    }

    @Test
    fun `disabling automatic saving cancels pending writes but allows manual save`() = runTest {
        val repository = TestResumeRepository()
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "draft", summary = "Pending"))

        saver.enabled = false
        saver.schedule(ResumeDocument(id = "draft", summary = "Disabled"))
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(repository.writes.isEmpty())

        saver.save(ResumeDocument(id = "draft", summary = "Manual"))
        assertEquals("Manual", repository.resumes.value.single().summary)
    }

    @Test
    fun `manual save cannot be overwritten by an older pending draft`() = runTest {
        val repository = TestResumeRepository()
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "draft", summary = "Older"))

        saver.save(ResumeDocument(id = "draft", summary = "Manual"))
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(listOf("Manual"), repository.writes.map { it.summary })
    }

    @Test
    fun `an in flight automatic save completes before a newer manual save`() = runTest {
        val repository = TestResumeRepository()
        val gate = CompletableDeferred<Unit>()
        repository.saveGate = gate
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "draft", summary = "Older"))
        saver.flush()
        runCurrent()

        val manualSave = launch { saver.save(ResumeDocument(id = "draft", summary = "Manual")) }
        runCurrent()
        gate.complete(Unit)
        manualSave.join()

        assertEquals(listOf("Older", "Manual"), repository.writes.map { it.summary })
        assertEquals("Manual", repository.resumes.value.single().summary)
    }

    @Test
    fun `deleting a resume prevents pending and queued saves from restoring it`() = runTest {
        val repository = TestResumeRepository()
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "draft", summary = "Queued"))
        saver.flush()
        saver.schedule(ResumeDocument(id = "draft", summary = "Pending"))

        saver.delete("draft")
        advanceTimeBy(1_000)
        runCurrent()

        assertTrue(repository.resumes.value.isEmpty())
    }

    @Test
    fun `flushing saves without waiting for the debounce`() = runTest {
        val repository = TestResumeRepository()
        val saver = ResumeAutoSaver(repository, backgroundScope) {}
        saver.schedule(ResumeDocument(id = "draft", summary = "Leaving app"))

        saver.flush()
        runCurrent()

        assertEquals("Leaving app", repository.resumes.value.single().summary)
    }

    @Test
    fun `automatic save failure is reported and the next edit can be saved`() = runTest {
        val repository = TestResumeRepository()
        var errors = 0
        val saver = ResumeAutoSaver(repository, backgroundScope) { errors++ }
        repository.failNextSave = true
        saver.schedule(ResumeDocument(id = "draft", summary = "Failed"))
        saver.flush()
        runCurrent()
        assertEquals(1, errors)

        saver.schedule(ResumeDocument(id = "draft", summary = "Recovered"))
        saver.flush()
        runCurrent()

        assertEquals("Recovered", repository.resumes.value.single().summary)
    }

    @Test
    fun `manual save failure reaches the caller without stopping later saves`() = runTest {
        val repository = TestResumeRepository()
        val saver = ResumeAutoSaver(repository, backgroundScope) { error("Manual error was reported as automatic") }
        repository.failNextSave = true

        val result = runCatching { saver.save(ResumeDocument(id = "draft")) }
        assertTrue(result.exceptionOrNull() is IOException)
        saver.save(ResumeDocument(id = "draft", summary = "Recovered"))
        assertEquals("Recovered", repository.resumes.value.single().summary)
    }
}

internal class TestResumeRepository : ResumeRepository {
    override var currentOwnerId: String? = null
    override val resumes = MutableStateFlow<List<ResumeDocument>>(emptyList())
    val writes = mutableListOf<ResumeDocument>()
    var failNextSave = false
    var saveGate: CompletableDeferred<Unit>? = null

    override suspend fun save(resume: ResumeDocument) {
        saveGate?.await()
        if (failNextSave) {
            failNextSave = false
            throw IOException("Test save failure")
        }
        writes += resume
        resumes.value = listOf(resume) + resumes.value.filterNot { it.id == resume.id }
    }

    override suspend fun delete(resumeId: String) {
        resumes.value = resumes.value.filterNot { it.id == resumeId }
    }
}

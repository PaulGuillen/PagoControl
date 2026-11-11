package com.devpaul.features.resume

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.SavedStateHandle
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResumeDraftAutoSaveTest {
    @Test
    fun `a restored draft from another account is discarded before automatic saving`() = runTest {
        val repository = TestResumeRepository().apply { currentOwnerId = "second" }
        val state = ResumeDraftState(SavedStateHandle(mapOf("resume_draft_owner" to "first")))
        store.put("draft", state)
        state.load(ResumeDocument(id = "private", summary = "First account"))
        state.configureAutoSave(repository, enabled = true)
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(state.document.summary.isEmpty())
        assertTrue(repository.writes.isEmpty())
    }

    private val store = ViewModelStore()

    @Before
    fun prepare() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun cleanUp() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun createState(repository: TestResumeRepository): ResumeDraftState = ResumeDraftState().also {
        store.put("draft", it)
        it.configureAutoSave(repository, enabled = true)
    }

    @Test
    fun `opening an empty or saved resume does not create an automatic save`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.startNew(emptySet())
        advanceTimeBy(1_000)
        runCurrent()
        state.load(ResumeDocument(id = "existing", summary = "Saved"))
        advanceTimeBy(1_000)
        runCurrent()

        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun `completed PDF import is saved without editing or continuing the form`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        val imported = ResumeDocument(contact = ResumeContact(name = "Imported", professionalTitle = "Developer"))

        state.importResume(imported)
        runCurrent()

        val saved = repository.resumes.value.single()
        assertEquals(imported.contact, saved.contact)
        assertFalse(saved.id.isBlank())
        assertEquals(saved.id, state.document.id)
        assertTrue(saved.updatedAt > 0)

        state.startNew(emptySet())
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(1, repository.resumes.value.size)
    }

    @Test
    fun `PDF import with automatic saving disabled waits for manual save`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.configureAutoSave(repository, enabled = false)

        state.importResume(ResumeDocument(contact = ResumeContact(name = "Imported")))
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(repository.writes.isEmpty())

        state.save()
        assertEquals("Imported", repository.resumes.value.single().contact.name)
    }

    @Test
    fun `enabling automatic saving also saves a previously imported PDF`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.configureAutoSave(repository, enabled = false)
        state.importResume(ResumeDocument(contact = ResumeContact(name = "Imported")))

        state.configureAutoSave(repository, enabled = true)
        advanceTimeBy(500)
        runCurrent()

        assertEquals("Imported", repository.resumes.value.single().contact.name)
    }

    @Test
    fun `incomplete contact fields are saved with one stable resume identifier`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.updatePending(ResumeDraftSection.Contact, state.document.copy(contact = ResumeContact(name = "Pa")))
        advanceTimeBy(500)
        runCurrent()
        val identifier = repository.resumes.value.single().id
        assertFalse(identifier.isBlank())
        assertTrue(state.document.contact.name.isEmpty())

        state.updatePending(ResumeDraftSection.Contact, state.document.copy(contact = ResumeContact(name = "Paul")))
        advanceTimeBy(500)
        runCurrent()

        assertEquals(identifier, repository.resumes.value.single().id)
        assertEquals("Paul", repository.resumes.value.single().contact.name)
        assertEquals(identifier, state.snapshotForSave().id)
    }

    @Test
    fun `canceling a pending edit restores the previous saved section`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.load(ResumeDocument(id = "existing", contact = ResumeContact(name = "Original")))
        state.updatePending(ResumeDraftSection.Contact, state.document.copy(contact = ResumeContact(name = "Pending")))
        state.flushAutoSave()
        runCurrent()

        state.updatePending(ResumeDraftSection.Contact, null)
        state.flushAutoSave()
        runCurrent()

        assertEquals("Original", repository.resumes.value.single().contact.name)
    }

    @Test
    fun `enabling automatic saving preserves edits made while it was disabled`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.configureAutoSave(repository, enabled = false)
        state.updateSummary("Pending changes")
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(repository.writes.isEmpty())

        state.configureAutoSave(repository, enabled = true)
        advanceTimeBy(500)
        runCurrent()

        assertEquals("Pending changes", repository.resumes.value.single().summary)
    }

    @Test
    fun `switching drafts flushes pending edits without copying them to the next resume`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.load(ResumeDocument(id = "first"))
        val previousSession = state.sessionId
        state.updatePending(ResumeDraftSection.Contact, state.document.copy(contact = ResumeContact(name = "Paul")))

        state.load(ResumeDocument(id = "second"))
        state.commitPending(ResumeDraftSection.Contact, previousSession)
        runCurrent()

        assertEquals("Paul", repository.resumes.value.single().contact.name)
        assertEquals("first", repository.resumes.value.single().id)
        assertEquals("second", state.document.id)
        assertTrue(state.document.contact.name.isEmpty())
    }

    @Test
    fun `deleting the active draft removes it without a delayed recreation`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        state.updateSummary("Draft")
        val identifier = state.document.id

        state.delete(identifier)
        advanceTimeBy(1_000)
        runCurrent()

        assertTrue(repository.resumes.value.isEmpty())
        assertTrue(state.document.id.isBlank())
    }

    @Test
    fun `reopening an active resume keeps newer edits over a stale list snapshot`() = runTest {
        val repository = TestResumeRepository()
        val state = createState(repository)
        val original = ResumeDocument(id = "existing", summary = "Original")
        state.load(original)
        state.updatePending(ResumeDraftSection.Summary, original.copy(summary = "Latest"))

        state.load(original)

        assertEquals("Latest", state.document.summary)
    }

    @Test
    fun `recreated draft restores incomplete fields and keeps the persisted resume identifier`() = runTest {
        val repository = TestResumeRepository()
        val savedState = SavedStateHandle()
        val original = ResumeDraftState(savedState)
        store.put("original", original)
        original.configureAutoSave(repository, enabled = true)
        original.updatePending(
            ResumeDraftSection.Contact,
            original.document.copy(contact = ResumeContact(name = "Partial")),
        )
        advanceTimeBy(500)
        runCurrent()
        val identifier = repository.resumes.value.single().id

        val restoredState = SavedStateHandle(savedState.keys().associateWith { savedState.get<Any?>(it) })
        val restored = ResumeDraftState(restoredState)
        store.put("restored", restored)
        restored.configureAutoSave(repository, enabled = true)
        assertEquals(identifier, restored.document.id)
        assertEquals("Partial", restored.document.contact.name)

        restored.updateContact(restored.document.contact.copy(name = "Completed"))
        advanceTimeBy(500)
        runCurrent()

        assertEquals(1, repository.resumes.value.size)
        assertEquals(identifier, repository.resumes.value.single().id)
        assertEquals("Completed", repository.resumes.value.single().contact.name)
    }
}

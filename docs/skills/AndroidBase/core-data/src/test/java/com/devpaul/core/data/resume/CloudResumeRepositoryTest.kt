package com.devpaul.core.data.resume

import com.devpaul.core.domain.resume.ResumeDocument
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CloudResumeRepositoryTest {
    @Test
    fun `account changes clear the previous list and sign out shows no resumes`() = runTest {
        val cloud = FakeCloudStore()
        cloud.snapshots.getValue("owner").value = ResumeCloudSnapshot(listOf(ResumeDocument(id = "private")))
        val repository = CloudResumeRepository(cloud, FakeLegacyStore(), backgroundScope)
        runCurrent()
        assertEquals("private", repository.resumes.value.single().id)

        cloud.userIds.value = "other"
        runCurrent()
        assertTrue(repository.resumes.value.isEmpty())
        cloud.userIds.value = null
        runCurrent()
        assertTrue(repository.resumes.value.isEmpty())
        assertFalse(repository.storageState.value.isLoading)
    }

    @Test
    fun `save and delete target the authenticated account without writing legacy data`() = runTest {
        val cloud = FakeCloudStore()
        val legacy = FakeLegacyStore()
        val repository = CloudResumeRepository(cloud, legacy, backgroundScope)
        runCurrent()
        repository.save(ResumeDocument(id = "new"))
        assertEquals("owner" to "new", cloud.saved.single())
        assertTrue(repository.storageState.value.hasPendingWrites)
        repository.delete("new")
        assertEquals("owner" to "new", cloud.deleted.single())
        assertTrue(legacy.documents.isEmpty())
    }

    @Test
    fun `signed out clients cannot save or delete`() = runTest {
        val cloud = FakeCloudStore().apply { userIds.value = null }
        val repository = CloudResumeRepository(cloud, FakeLegacyStore(), backgroundScope)
        assertTrue(runCatching { repository.save(ResumeDocument(id = "new")) }.isFailure)
        assertTrue(runCatching { repository.delete("new") }.isFailure)
        assertTrue(cloud.saved.isEmpty())
        assertTrue(cloud.deleted.isEmpty())
    }

    @Test
    fun `local resumes are offered for explicit migration and never uploaded automatically`() = runTest {
        val cloud = FakeCloudStore()
        val legacy = FakeLegacyStore(mutableListOf(ResumeDocument(id = "old")))
        val repository = CloudResumeRepository(cloud, legacy, backgroundScope)
        runCurrent()
        assertTrue(repository.storageState.value.hasLocalResumes)
        assertTrue(repository.resumes.value.isEmpty())
        assertTrue(cloud.imported.isEmpty())
        assertEquals(null, legacy.owner)
    }

    @Test
    fun `migration removes legacy data only after the server acknowledges it`() = runTest {
        val cloud = FakeCloudStore()
        val legacy = FakeLegacyStore(mutableListOf(ResumeDocument(id = "old", summary = "Keep me")))
        val repository = CloudResumeRepository(cloud, legacy, backgroundScope)
        runCurrent()
        cloud.importFailure = IOException("Offline")
        assertTrue(runCatching { repository.migrateLocalResumes() }.isFailure)
        assertEquals(1, legacy.documents.size)
        assertEquals("owner", legacy.owner)
        assertTrue(repository.storageState.value.hasError)

        cloud.importFailure = null
        repository.migrateLocalResumes()
        assertTrue(legacy.documents.isEmpty())
        assertEquals("Keep me", cloud.imported.values.single().summary)
        assertFalse(repository.storageState.value.hasLocalResumes)
    }

    @Test
    fun `retry after a local cleanup failure does not duplicate or overwrite a migrated resume`() = runTest {
        val cloud = FakeCloudStore()
        val legacy = FakeLegacyStore(mutableListOf(ResumeDocument(id = "old", summary = "Original")))
        legacy.failRemove = true
        val repository = CloudResumeRepository(cloud, legacy, backgroundScope)
        runCurrent()
        assertTrue(runCatching { repository.migrateLocalResumes() }.isFailure)
        assertEquals(1, cloud.imported.size)
        val key = "owner" to legacyDocumentId("old")
        cloud.imported[key] = cloud.imported.getValue(key).copy(summary = "Edited on another device")

        legacy.failRemove = false
        repository.migrateLocalResumes()
        assertEquals(1, cloud.imported.size)
        assertEquals("Edited on another device", cloud.imported.getValue(key).summary)
        assertTrue(legacy.documents.isEmpty())
    }

    @Test
    fun `another account cannot claim an interrupted migration`() = runTest {
        val cloud = FakeCloudStore()
        val legacy = FakeLegacyStore(mutableListOf(ResumeDocument(id = "old"))).apply { owner = "other" }
        val repository = CloudResumeRepository(cloud, legacy, backgroundScope)
        runCurrent()
        assertFalse(repository.storageState.value.hasLocalResumes)
        assertTrue(runCatching { repository.migrateLocalResumes() }.isFailure)
        assertTrue(cloud.imported.isEmpty())
        assertEquals(1, legacy.documents.size)
    }

    @Test
    fun `listener failures are visible and retry reconnects without crashing collectors`() = runTest {
        val cloud = FakeCloudStore().apply { listenerFailure = IOException("Permission denied") }
        val repository = CloudResumeRepository(cloud, FakeLegacyStore(), backgroundScope)
        runCurrent()
        assertTrue(repository.storageState.value.hasError)
        cloud.listenerFailure = null
        repository.retrySync()
        runCurrent()
        assertFalse(repository.storageState.value.hasError)
        assertFalse(repository.storageState.value.isLoading)
    }

    @Test
    fun `offline metadata is not reported as a confirmed cloud backup`() = runTest {
        val cloud = FakeCloudStore()
        cloud.snapshots.getValue("owner").value = ResumeCloudSnapshot(emptyList(), hasPendingWrites = true, isFromCache = true)
        val repository = CloudResumeRepository(cloud, FakeLegacyStore(), backgroundScope)
        runCurrent()
        assertTrue(repository.storageState.value.isFromCache)
        assertTrue(repository.storageState.value.hasPendingWrites)

        cloud.snapshots.getValue("owner").value = ResumeCloudSnapshot(emptyList())
        runCurrent()
        assertFalse(repository.storageState.value.isFromCache)
        assertFalse(repository.storageState.value.hasPendingWrites)
    }

    @Test
    fun `rejected asynchronous writes expose a synchronization error`() = runTest {
        val cloud = FakeCloudStore()
        val repository = CloudResumeRepository(cloud, FakeLegacyStore(), backgroundScope)
        runCurrent()
        repository.save(ResumeDocument(id = "new"))
        cloud.writeFailure?.invoke(IOException("Permission denied"))
        assertTrue(repository.storageState.value.hasError)
    }
}

private class FakeCloudStore : ResumeCloudStore {
    override val userIds = MutableStateFlow<String?>("owner")
    override val currentUid: String? get() = userIds.value
    val snapshots = mapOf(
        "owner" to MutableStateFlow(ResumeCloudSnapshot(emptyList())),
        "other" to MutableStateFlow(ResumeCloudSnapshot(emptyList())),
    )
    val saved = mutableListOf<Pair<String, String>>()
    val deleted = mutableListOf<Pair<String, String>>()
    val imported = mutableMapOf<Pair<String, String>, ResumeDocument>()
    var importFailure: Exception? = null
    var listenerFailure: Exception? = null
    var writeFailure: ((Exception) -> Unit)? = null

    override fun observe(uid: String): Flow<ResumeCloudSnapshot> = listenerFailure?.let { error ->
        flow { throw error }
    } ?: snapshots.getValue(uid)

    override fun save(uid: String, resume: ResumeDocument, onFailure: (Exception) -> Unit) {
        saved += uid to resume.id
        writeFailure = onFailure
    }

    override fun delete(uid: String, resumeId: String, onFailure: (Exception) -> Unit) {
        deleted += uid to resumeId
        writeFailure = onFailure
    }

    override suspend fun importIfAbsent(uid: String, resume: ResumeDocument) {
        importFailure?.let { throw it }
        imported.putIfAbsent(uid to resume.id, resume)
    }
}

private class FakeLegacyStore(
    val documents: MutableList<ResumeDocument> = mutableListOf(),
) : LegacyResumeSource {
    var owner: String? = null
    var failRemove = false
    override suspend fun ownerUid() = owner
    override suspend fun read() = documents.toList()
    override suspend fun claim(uid: String) {
        check(owner == null || owner == uid)
        owner = uid
    }
    override suspend fun remove(resumeId: String) {
        if (failRemove) throw IOException("Local disk failure")
        documents.removeAll { it.id == resumeId }
    }
}

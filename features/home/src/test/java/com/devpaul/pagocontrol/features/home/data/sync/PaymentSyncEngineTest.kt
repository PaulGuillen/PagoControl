package com.devpaul.pagocontrol.features.home.data.sync

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.PaymentSyncCheckpoint
import com.devpaul.pagocontrol.features.home.data.datasource.remote.PaymentCloudStore
import com.devpaul.pagocontrol.features.home.data.datasource.remote.model.PaymentCloudBatch
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentSyncEngineTest {
    private fun row(id: String, revision: Long = 0, ownerId: String = "alice") = NotificationEntity(
        id = id, packageName = "com.google.android.gm", source = "Gmail", title = "Bank",
        text = "Receipt", postedAt = 1, suggestedAmountCents = 100,
        amountCents = 100, status = "confirmed", ownerId = ownerId,
        pendingWriteId = if (revision == 0L) "write-$id" else null, remoteRevision = revision,
    )

    @Test
    fun initialDownloadIsPagedAndSecondVisitDoesNotDownloadHistory() = runTest {
        val local = Local()
        val cloud = Cloud((1..250).map { row(id = "$it", revision = it.toLong()) })
        val engine = PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { true })
        engine.sync(ownerId = "alice", checkRemote = true)
        assertEquals(250, local.rows.size)
        assertEquals(listOf(0L, 100L, 200L), cloud.requestedAfter)
        assertTrue(local.checkpoint("alice").initialDownloadComplete)
        engine.sync(ownerId = "alice", checkRemote = true)
        assertEquals(3, cloud.requestedAfter.size)
        engine.sync(ownerId = "alice", checkRemote = false)
        assertEquals(2, cloud.headReads)
    }

    @Test
    fun localWriteIsAcknowledgedWithoutDownloadingItsOwnDocument() = runTest {
        val local = Local()
        val cloud = Cloud(emptyList())
        val engine = PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { true })
        engine.sync(ownerId = "alice", checkRemote = true)
        local.rows["new"] = row("new")
        engine.sync(ownerId = "alice", checkRemote = false)
        assertTrue(cloud.requestedAfter.isEmpty())
        assertEquals(1, cloud.headReads)
        assertEquals(1, cloud.writes)
        assertEquals(null, local.rows.getValue("new").pendingWriteId)
        assertEquals(1L, local.checkpoint("alice").revision)
    }

    @Test
    fun interruptedBootstrapResumesFromLastCommittedPage() = runTest {
        val local = Local()
        val cloud = Cloud((1..230).map { row(id = "$it", revision = it.toLong()) })
        cloud.failAfter = 100
        val engine = PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { true })
        try {
            engine.sync(ownerId = "alice", checkRemote = true)
        } catch (_: IOException) {
        }
        assertEquals(100L, local.checkpoint("alice").revision)
        assertFalse(local.checkpoint("alice").initialDownloadComplete)
        cloud.failAfter = null
        engine.sync(ownerId = "alice", checkRemote = true)
        assertEquals(listOf(0L, 100L, 100L, 200L), cloud.requestedAfter)
        assertEquals(230, local.rows.size)
    }

    @Test
    fun failedUploadKeepsOutboxAndRetryDoesNotDuplicate() = runTest {
        val local = Local()
        local.rows["new"] = row("new")
        val cloud = Cloud(emptyList())
        cloud.loseAcknowledgement = true
        val engine = PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { true })
        try {
            engine.sync(ownerId = "alice", checkRemote = true)
        } catch (_: IOException) {
        }
        assertTrue(local.rows.getValue("new").pendingWriteId != null)
        engine.sync(ownerId = "alice", checkRemote = true)
        assertEquals(1, cloud.writes)
        assertEquals(null, local.rows.getValue("new").pendingWriteId)
    }

    @Test
    fun incrementalDownloadIncludesEditsAndTombstones() = runTest {
        val local = Local()
        val cloud = Cloud(listOf(row(id = "a", revision = 1)))
        val engine = PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { true })
        engine.sync(ownerId = "alice", checkRemote = true)
        cloud.rows["a"] =
            row(id = "a", revision = 2).copy(status = "discarded", title = "", text = "")
        cloud.rows["b"] = row(id = "b", revision = 3).copy(amountCents = -100)
        engine.sync(ownerId = "alice", checkRemote = true)
        assertEquals(listOf(0L, 1L), cloud.requestedAfter)
        assertEquals("discarded", local.rows.getValue("a").status)
        assertEquals(-100L, local.rows.getValue("b").amountCents)
    }

    @Test
    fun accountSwitchDuringDownloadCannotApplyAnotherUsersPage() = runTest {
        val local = Local()
        val cloud = Cloud(listOf(row(id = "a", revision = 1)))
        var current = "alice"
        cloud.onPage = { current = "bob" }
        val engine =
            PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { it == current })
        try {
            engine.sync(ownerId = "alice", checkRemote = true)
        } catch (_: IllegalStateException) {
        }
        assertTrue(local.rows.isEmpty())
        assertEquals(0L, local.checkpoint("alice").revision)
    }

    @Test
    fun newerLocalEditDuringUploadRemainsQueuedAndIsSentNext() = runTest {
        val local = Local()
        local.rows["new"] = row("new")
        val cloud = Cloud(emptyList())
        cloud.onSave = {
            local.rows["new"] =
                local.rows.getValue("new").copy(amountCents = -200, pendingWriteId = "newer")
            cloud.onSave = null
        }
        PaymentSyncEngine(
            local = local,
            cloud = cloud,
            isCurrentOwner = { true }).sync(ownerId = "alice", checkRemote = true)
        assertEquals(2, cloud.writes)
        assertEquals(-200L, cloud.rows.getValue("new").amountCents)
        assertEquals(null, local.rows.getValue("new").pendingWriteId)
    }

    @Test
    fun changesFromAnotherDeviceArePulledBeforeAdvancingPastAnUpload() = runTest {
        val local = Local()
        val cloud = Cloud(emptyList())
        val engine = PaymentSyncEngine(local = local, cloud = cloud, isCurrentOwner = { true })
        engine.sync(ownerId = "alice", checkRemote = true)
        cloud.rows["remote"] = row(id = "remote", revision = 1)
        local.rows["local"] = row("local")
        engine.sync(ownerId = "alice", checkRemote = false)
        assertEquals(setOf("remote", "local"), local.rows.keys)
        assertEquals(2L, local.checkpoint("alice").revision)
    }

    private class Local : PaymentSyncLocalStore {
        val rows = mutableMapOf<String, NotificationEntity>()
        private val checkpoints = mutableMapOf<String, PaymentSyncCheckpoint>()
        override suspend fun prepare(ownerId: String) {}
        override suspend fun checkpoint(ownerId: String) =
            checkpoints[ownerId] ?: PaymentSyncCheckpoint(ownerId)

        override suspend fun pending(ownerId: String) =
            rows.values.filter { it.ownerId == ownerId && it.pendingWriteId != null }.take(20)

        override suspend fun applyPage(
            ownerId: String,
            rows: List<NotificationEntity>,
            revision: Long
        ) {
            rows.forEach { if (this.rows[it.id]?.pendingWriteId == null) this.rows[it.id] = it }
            checkpoints[ownerId] = checkpoint(ownerId).copy(revision = revision)
        }

        override suspend fun acknowledge(sent: NotificationEntity, remote: NotificationEntity) {
            val current = rows.getValue(sent.id)
            rows[sent.id] = if (current.pendingWriteId == sent.pendingWriteId) remote
            else current.copy(remoteRevision = remote.remoteRevision)
        }

        override suspend fun complete(ownerId: String) {
            checkpoints[ownerId] = checkpoint(ownerId).copy(initialDownloadComplete = true)
        }
    }

    private class Cloud(initial: List<NotificationEntity>) : PaymentCloudStore {
        val rows = initial.associateBy { it.id }.toMutableMap()
        val writeIds = mutableMapOf<String, String?>()
        val requestedAfter = mutableListOf<Long>()
        var headReads = 0
        var writes = 0
        var failAfter: Long? = null
        var loseAcknowledgement = false
        var onPage: (() -> Unit)? = null
        var onSave: (() -> Unit)? = null
        private fun revision() = rows.values.maxOfOrNull { it.remoteRevision } ?: 0L
        override suspend fun head(ownerId: String): Long {
            headReads++; return revision()
        }

        override suspend fun changes(
            ownerId: String,
            after: Long,
            through: Long,
            limit: Int
        ): List<NotificationEntity> {
            requestedAfter += after
            if (failAfter == after) throw IOException("Offline")
            onPage?.invoke()
            return rows.values.filter { it.remoteRevision > after && it.remoteRevision <= through }
                .sortedBy { it.remoteRevision }.take(limit)
        }

        override suspend fun save(
            ownerId: String,
            rows: List<NotificationEntity>
        ): PaymentCloudBatch {
            val base = revision()
            var revision = base
            val remote = rows.map { row ->
                if (writeIds[row.id] == row.pendingWriteId) this.rows.getValue(row.id) else {
                    writes++
                    row.copy(pendingWriteId = null, remoteRevision = ++revision).also {
                        this.rows[row.id] = it
                        writeIds[row.id] = row.pendingWriteId
                    }
                }
            }
            onSave?.invoke()
            if (loseAcknowledgement) {
                loseAcknowledgement = false; throw IOException("Lost response after commit")
            }
            return PaymentCloudBatch(baseRevision = base, revision = revision, rows = remote)
        }
    }
}

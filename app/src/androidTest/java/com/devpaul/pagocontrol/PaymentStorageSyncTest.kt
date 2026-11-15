package com.devpaul.pagocontrol

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabaseMigrations
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentStorageSyncTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun row(ownerId: String, id: String = "same") = NotificationEntity(
        id = id, packageName = "com.google.android.gm", source = "Gmail", title = "Bank",
        text = "Receipt", postedAt = 1, suggestedAmountCents = 100, ownerId = ownerId,
    )

    @Test
    fun accountQueriesAndMutationsCannotCrossOwners() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val dao = database.notifications()
            dao.insert(row("alice"))
            dao.insert(row("bob"))
            dao.confirm(id = "same", amountCents = 100, ownerId = "alice")
            assertEquals(1, dao.confirmed(mail = true, ownerId = "alice").size)
            assertTrue(dao.confirmed(mail = true, ownerId = "bob").isEmpty())
            dao.discard(id = "same", ownerId = "bob")
            assertEquals("confirmed", dao.find(id = "same", ownerId = "alice")?.status)
            assertEquals("discarded", dao.find(id = "same", ownerId = "bob")?.status)
        } finally {
            database.close()
        }
    }

    @Test
    fun remotePagesDoNotOverwritePendingEditsAndAcknowledgementKeepsANewerWrite() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val dao = database.notifications()
            val sent = row("alice")
            dao.insert(sent)
            val remote = sent.copy(
                status = "confirmed",
                amountCents = 100,
                pendingWriteId = null,
                remoteRevision = 5
            )
            dao.applyRemotePage(ownerId = "alice", rows = listOf(remote), revision = 5)
            assertEquals("pending", dao.find(id = "same", ownerId = "alice")?.status)
            dao.confirm(id = "same", amountCents = -200, ownerId = "alice")
            dao.acknowledgeWrite(sent = sent, remote = remote)
            val newer = requireNotNull(dao.find(id = "same", ownerId = "alice"))
            assertEquals(-200L, newer.amountCents)
            assertTrue(newer.pendingWriteId != null)
            assertEquals(5L, newer.remoteRevision)
            dao.acknowledgeWrite(
                sent = newer,
                remote = newer.copy(pendingWriteId = null, remoteRevision = 6)
            )
            assertTrue(dao.pendingWrites("alice").isEmpty())
        } finally {
            database.close()
        }
    }

    @Test
    fun bootstrapCheckpointAndOutboxSurviveDatabaseReopen() = runBlocking {
        val name = "sync-test-${UUID.randomUUID()}.db"
        fun open() = Room.databaseBuilder(context, PaymentDatabase::class.java, name).build()
        var database = open()
        try {
            database.notifications().insert(row("alice"))
            database.notifications()
                .applyRemotePage(ownerId = "alice", rows = emptyList(), revision = 123)
            database.close()
            database = open()
            assertEquals(123L, database.notifications().syncCheckpoint("alice")?.revision)
            assertEquals(1, database.notifications().pendingWrites("alice").size)
            assertTrue(database.notifications().pendingWrites("bob").isEmpty())
        } finally {
            database.close(); context.deleteDatabase(name)
        }
    }

    @Test
    fun versionOneMigrationPreservesDataAndClaimsLegacyOnlyOnce() = runBlocking {
        val name = "migration-test-${UUID.randomUUID()}.db"
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL("CREATE TABLE payment_notifications (id TEXT NOT NULL PRIMARY KEY, packageName TEXT NOT NULL, source TEXT NOT NULL, title TEXT NOT NULL, text TEXT NOT NULL, postedAt INTEGER NOT NULL, suggestedAmountCents INTEGER, amountCents INTEGER, status TEXT NOT NULL)")
            old.execSQL("CREATE TABLE notification_sources (packageName TEXT NOT NULL PRIMARY KEY)")
            old.execSQL("INSERT INTO payment_notifications VALUES ('legacy', 'com.google.android.gm', 'Gmail', 'Bank', 'Receipt', 123, 100, 100, 'confirmed')")
            old.execSQL("INSERT INTO notification_sources VALUES ('com.google.android.gm')")
            old.version = 1
        }
        val database = Room.databaseBuilder(context, PaymentDatabase::class.java, name)
            .addMigrations(PaymentDatabaseMigrations.FROM_1_TO_2).build()
        try {
            val dao = database.notifications()
            dao.claimLegacy("alice")
            assertEquals(100L, dao.find(id = "legacy", ownerId = "alice")?.amountCents)
            assertEquals(1, dao.pendingWrites("alice").size)
            assertTrue(dao.observeSources("alice").first().contains("com.google.android.gm"))
            dao.claimLegacy("bob")
            assertEquals(null, dao.find(id = "legacy", ownerId = "bob"))
            assertFalse(dao.observeSources("bob").first().contains("com.google.android.gm"))
            assertEquals(null, dao.find(id = "legacy", ownerId = ""))
        } finally {
            database.close(); context.deleteDatabase(name)
        }
    }
}

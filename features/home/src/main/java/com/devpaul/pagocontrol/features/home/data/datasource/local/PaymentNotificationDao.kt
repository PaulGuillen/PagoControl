package com.devpaul.pagocontrol.features.home.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationSourceEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.PaymentSyncCheckpoint
import com.devpaul.pagocontrol.features.home.data.mapper.asPaymentNotification
import com.devpaul.pagocontrol.features.home.domain.recognition.AutomaticGmailPayment
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentNotificationDao {
    @Query("SELECT * FROM payment_notifications WHERE ownerId = :ownerId AND status IN ('pending', 'transfer', 'confirmed', 'automatic') AND (packageName = 'com.google.android.gm') = :mail ORDER BY postedAt DESC")
    fun observePending(mail: Boolean = false, ownerId: String = ""): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM payment_notifications WHERE ownerId = :ownerId AND status IN ('confirmed', 'automatic', 'transfer') AND (packageName = 'com.google.android.gm') = :mail ORDER BY postedAt DESC")
    fun observeConfirmed(
        mail: Boolean = false,
        ownerId: String = ""
    ): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM payment_notifications WHERE ownerId = :ownerId AND status IN ('confirmed', 'automatic', 'transfer') AND (packageName = 'com.google.android.gm') = :mail ORDER BY postedAt DESC")
    suspend fun confirmed(mail: Boolean = false, ownerId: String = ""): List<NotificationEntity>

    @Query("SELECT packageName FROM notification_sources WHERE ownerId = :ownerId ORDER BY position, packageName")
    fun observeSources(ownerId: String = ""): Flow<List<String>>

    @Query("SELECT * FROM payment_notifications WHERE ownerId = :ownerId AND packageName = 'com.google.android.gm' AND status = 'pending'")
    suspend fun pendingGmail(ownerId: String = ""): List<NotificationEntity>

    @Query("UPDATE payment_notifications SET status = 'automatic', amountCents = :amountCents, pendingWriteId = :writeId WHERE ownerId = :ownerId AND id = :id AND status = 'pending'")
    suspend fun recordAutomatic(
        id: String,
        amountCents: Long,
        ownerId: String = "",
        writeId: String = UUID.randomUUID().toString()
    )

    @Query("SELECT EXISTS(SELECT 1 FROM notification_sources WHERE ownerId = :ownerId AND packageName = :packageName)")
    suspend fun isEnabled(packageName: String, ownerId: String = ""): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity): Long

    @Transaction
    suspend fun captureIfEnabled(notification: NotificationEntity): NotificationEntity? {
        if (!isEnabled(
                packageName = notification.packageName,
                ownerId = notification.ownerId
            )
        ) return null
        val mail = notification.packageName == "com.google.android.gm"
        val duplicate = mail && confirmed(mail = true, ownerId = notification.ownerId).any {
            AutomaticGmailPayment.possibleDuplicate(
                notification.asPaymentNotification(), it.asPaymentNotification(), it.amountCents
            )
        }
        val recorded = if (duplicate) notification.copy(
            status = "pending",
            amountCents = null
        ) else notification
        return recorded.takeIf { insert(it) != -1L && it.status == "automatic" }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSource(source: NotificationSourceEntity)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM notification_sources WHERE ownerId = :ownerId")
    suspend fun nextSourcePosition(ownerId: String): Long

    @Transaction
    suspend fun enable(source: NotificationSourceEntity) {
        insertSource(source.copy(position = nextSourcePosition(source.ownerId)))
    }

    @Query("DELETE FROM notification_sources WHERE ownerId = :ownerId AND packageName = :packageName")
    suspend fun disable(packageName: String, ownerId: String = "")

    @Query("UPDATE payment_notifications SET status = 'confirmed', amountCents = :amountCents, pendingWriteId = :writeId WHERE ownerId = :ownerId AND id = :id AND status IN ('pending', 'transfer')")
    suspend fun confirm(
        id: String,
        amountCents: Long,
        ownerId: String = "",
        writeId: String = UUID.randomUUID().toString()
    ): Int

    @Query("UPDATE payment_notifications SET status = 'transfer', amountCents = :amountCents, pendingWriteId = :writeId WHERE ownerId = :ownerId AND id = :id AND status IN ('pending', 'confirmed', 'automatic')")
    suspend fun markOwnTransfer(
        id: String,
        amountCents: Long,
        ownerId: String,
        writeId: String = UUID.randomUUID().toString()
    )

    @Query("SELECT * FROM payment_notifications WHERE ownerId = :ownerId AND id = :id")
    suspend fun find(id: String, ownerId: String = ""): NotificationEntity?

    @Transaction
    suspend fun confirmMovement(
        id: String,
        amountCents: Long,
        ownerId: String = ""
    ): NotificationEntity? =
        if (confirm(id = id, amountCents = amountCents, ownerId = ownerId) == 1) find(
            id = id,
            ownerId = ownerId
        ) else null

    @Query("UPDATE payment_notifications SET status = 'discarded', title = '', text = '', pendingWriteId = :writeId WHERE ownerId = :ownerId AND id = :id AND (status = 'pending' OR (packageName = 'com.google.android.gm' AND status IN ('confirmed', 'automatic', 'transfer')))")
    suspend fun discard(
        id: String,
        ownerId: String = "",
        writeId: String = UUID.randomUUID().toString()
    )

    @Query("SELECT * FROM payment_notifications WHERE ownerId = :ownerId AND pendingWriteId IS NOT NULL ORDER BY postedAt, id LIMIT :limit")
    suspend fun pendingWrites(ownerId: String, limit: Int = 20): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM payment_notifications WHERE ownerId = :ownerId AND pendingWriteId IS NOT NULL")
    fun observePendingWriteCount(ownerId: String): Flow<Int>

    @Query("SELECT pendingWriteId FROM payment_notifications WHERE ownerId = :ownerId AND pendingWriteId IS NOT NULL ORDER BY id")
    fun observeWriteTokens(ownerId: String): Flow<List<String>>

    @Query("SELECT * FROM payment_sync_state WHERE ownerId = :ownerId")
    suspend fun syncCheckpoint(ownerId: String): PaymentSyncCheckpoint?

    @Upsert
    suspend fun saveCheckpoint(checkpoint: PaymentSyncCheckpoint)

    @Upsert
    suspend fun upsertRemote(row: NotificationEntity)

    @Transaction
    suspend fun applyRemotePage(ownerId: String, rows: List<NotificationEntity>, revision: Long) {
        rows.forEach { row ->
            require(row.ownerId == ownerId && row.pendingWriteId == null)
            val current = find(id = row.id, ownerId = ownerId)
            if (current == null || (current.pendingWriteId == null && row.remoteRevision >= current.remoteRevision)) {
                upsertRemote(row)
            }
        }
        val previous = syncCheckpoint(ownerId) ?: PaymentSyncCheckpoint(ownerId)
        saveCheckpoint(previous.copy(revision = maxOf(previous.revision, revision)))
    }

    @Transaction
    suspend fun acknowledgeWrite(sent: NotificationEntity, remote: NotificationEntity) {
        require(sent.ownerId == remote.ownerId && sent.id == remote.id)
        val current = find(id = sent.id, ownerId = sent.ownerId) ?: return
        when {
            current.pendingWriteId == sent.pendingWriteId -> upsertRemote(remote.copy(pendingWriteId = null))
            current.pendingWriteId != null -> upsertRemote(
                current.copy(
                    remoteRevision = maxOf(
                        current.remoteRevision,
                        remote.remoteRevision
                    )
                )
            )
        }
    }

    // Legacy rows belonged to this installation. Claim them once, never on every account switch.
    @Query("INSERT OR IGNORE INTO payment_notifications (id, packageName, source, title, text, postedAt, suggestedAmountCents, amountCents, status, ownerId, pendingWriteId, remoteRevision) SELECT id, packageName, source, title, text, postedAt, suggestedAmountCents, amountCents, status, :ownerId, pendingWriteId, remoteRevision FROM payment_notifications WHERE ownerId = ''")
    suspend fun copyLegacyMovements(ownerId: String)

    @Query("INSERT OR IGNORE INTO notification_sources (packageName, ownerId, position) SELECT packageName, :ownerId, position FROM notification_sources WHERE ownerId = ''")
    suspend fun copyLegacySources(ownerId: String)

    @Query("DELETE FROM payment_notifications WHERE ownerId = ''")
    suspend fun removeLegacyMovements()

    @Query("DELETE FROM notification_sources WHERE ownerId = ''")
    suspend fun removeLegacySources()

    @Transaction
    suspend fun claimLegacy(ownerId: String) {
        if (ownerId.isEmpty()) return
        copyLegacyMovements(ownerId)
        copyLegacySources(ownerId)
        removeLegacyMovements()
        removeLegacySources()
    }
}

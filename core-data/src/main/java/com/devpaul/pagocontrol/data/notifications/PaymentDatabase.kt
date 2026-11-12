package com.devpaul.pagocontrol.data.notifications

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "payment_notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val source: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val suggestedAmountCents: Long?,
    val amountCents: Long? = null,
    val status: String = "pending",
)

@Entity(tableName = "notification_sources")
data class NotificationSourceEntity(@PrimaryKey val packageName: String)

@Dao
interface PaymentNotificationDao {
    @Query("SELECT * FROM payment_notifications WHERE status = 'pending' AND (packageName = 'com.google.android.gm') = :mail ORDER BY postedAt DESC")
    fun observePending(mail: Boolean = false): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM payment_notifications WHERE status IN ('confirmed', 'automatic') AND (packageName = 'com.google.android.gm') = :mail ORDER BY postedAt DESC")
    fun observeConfirmed(mail: Boolean = false): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM payment_notifications WHERE status IN ('confirmed', 'automatic') AND (packageName = 'com.google.android.gm') = :mail ORDER BY postedAt DESC")
    suspend fun confirmed(mail: Boolean = false): List<NotificationEntity>

    @Query("SELECT packageName FROM notification_sources")
    fun observeSources(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM notification_sources WHERE packageName = :packageName)")
    suspend fun isEnabled(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity)

    @Transaction
    suspend fun captureIfEnabled(notification: NotificationEntity) {
        if (isEnabled(notification.packageName)) insert(notification)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enable(source: NotificationSourceEntity)

    @Query("DELETE FROM notification_sources WHERE packageName = :packageName")
    suspend fun disable(packageName: String)

    @Query("UPDATE payment_notifications SET status = 'confirmed', amountCents = :amountCents WHERE id = :id AND status = 'pending'")
    suspend fun confirm(id: String, amountCents: Long)

    @Query("UPDATE payment_notifications SET status = 'discarded', title = '', text = '' WHERE id = :id AND status = 'pending'")
    suspend fun discard(id: String)
}

@Database(entities = [NotificationEntity::class, NotificationSourceEntity::class], version = 1)
abstract class PaymentDatabase : RoomDatabase() {
    abstract fun notifications(): PaymentNotificationDao
}

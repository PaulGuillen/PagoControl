package com.devpaul.pagocontrol.features.home.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationSourceEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.PaymentSyncCheckpoint

@Database(
    entities = [NotificationEntity::class, NotificationSourceEntity::class, PaymentSyncCheckpoint::class],
    version = 3
)
abstract class PaymentDatabase : RoomDatabase() {
    abstract fun notifications(): PaymentNotificationDao
}

package com.devpaul.pagocontrol.features.home.data.sync

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.PaymentSyncCheckpoint

internal interface PaymentSyncLocalStore {
    suspend fun prepare(ownerId: String)
    suspend fun checkpoint(ownerId: String): PaymentSyncCheckpoint
    suspend fun pending(ownerId: String): List<NotificationEntity>
    suspend fun applyPage(ownerId: String, rows: List<NotificationEntity>, revision: Long)
    suspend fun acknowledge(sent: NotificationEntity, remote: NotificationEntity)
    suspend fun complete(ownerId: String)
}

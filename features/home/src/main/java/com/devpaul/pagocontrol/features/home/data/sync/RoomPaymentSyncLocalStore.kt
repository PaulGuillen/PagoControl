package com.devpaul.pagocontrol.features.home.data.sync

import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentNotificationDao
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.PaymentSyncCheckpoint

internal class RoomPaymentSyncLocalStore(private val dao: PaymentNotificationDao) :
    PaymentSyncLocalStore {
    override suspend fun prepare(ownerId: String) = dao.claimLegacy(ownerId)
    override suspend fun checkpoint(ownerId: String) =
        dao.syncCheckpoint(ownerId) ?: PaymentSyncCheckpoint(ownerId)

    override suspend fun pending(ownerId: String) = dao.pendingWrites(ownerId)
    override suspend fun applyPage(
        ownerId: String,
        rows: List<NotificationEntity>,
        revision: Long
    ) =
        dao.applyRemotePage(ownerId = ownerId, rows = rows, revision = revision)

    override suspend fun acknowledge(sent: NotificationEntity, remote: NotificationEntity) =
        dao.acknowledgeWrite(sent = sent, remote = remote)

    override suspend fun complete(ownerId: String) =
        dao.saveCheckpoint(checkpoint(ownerId).copy(initialDownloadComplete = true))
}

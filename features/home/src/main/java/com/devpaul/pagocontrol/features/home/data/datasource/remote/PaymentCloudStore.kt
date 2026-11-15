package com.devpaul.pagocontrol.features.home.data.datasource.remote

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.remote.model.PaymentCloudBatch

internal interface PaymentCloudStore {
    suspend fun head(ownerId: String): Long
    suspend fun changes(
        ownerId: String,
        after: Long,
        through: Long,
        limit: Int
    ): List<NotificationEntity>

    suspend fun save(ownerId: String, rows: List<NotificationEntity>): PaymentCloudBatch
}

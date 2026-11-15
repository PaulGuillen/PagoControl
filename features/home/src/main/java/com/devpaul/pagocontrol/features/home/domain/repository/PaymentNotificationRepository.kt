package com.devpaul.pagocontrol.features.home.domain.repository

import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import kotlinx.coroutines.flow.Flow

interface PaymentNotificationRepository {
    fun observePending(): Flow<List<PaymentNotification>>

    fun observeSources(): Flow<Set<String>>

    suspend fun setSource(packageName: String, enabled: Boolean)

    suspend fun capture(notification: PaymentNotification)

    suspend fun confirm(id: String, amountCents: Long)

    suspend fun discard(id: String)
}

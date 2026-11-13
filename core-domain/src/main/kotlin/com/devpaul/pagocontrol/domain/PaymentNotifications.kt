package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

data class PaymentNotification(
    val id: String,
    val packageName: String,
    val source: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val suggestedAmountCents: Long?,
    val possibleDuplicate: Boolean = false,
)

interface PaymentNotificationRepository {
    fun observePending(): Flow<List<PaymentNotification>>

    fun observeSources(): Flow<Set<String>>

    suspend fun setSource(packageName: String, enabled: Boolean)

    suspend fun capture(notification: PaymentNotification)

    suspend fun confirm(id: String, amountCents: Long)

    suspend fun discard(id: String)
}

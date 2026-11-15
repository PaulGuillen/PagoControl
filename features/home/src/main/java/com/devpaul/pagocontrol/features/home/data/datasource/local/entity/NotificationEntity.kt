package com.devpaul.pagocontrol.features.home.data.datasource.local.entity

import androidx.room.Entity
import java.util.UUID

@Entity(tableName = "payment_notifications", primaryKeys = ["ownerId", "id"])
data class NotificationEntity(
    val id: String,
    val packageName: String,
    val source: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val suggestedAmountCents: Long?,
    val amountCents: Long? = null,
    val status: String = "pending",
    val ownerId: String = "",
    val pendingWriteId: String? = UUID.randomUUID().toString(),
    val remoteRevision: Long = 0,
)

package com.devpaul.pagocontrol.features.home.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_sync_state")
data class PaymentSyncCheckpoint(
    @PrimaryKey val ownerId: String,
    val revision: Long = 0,
    val initialDownloadComplete: Boolean = false,
)

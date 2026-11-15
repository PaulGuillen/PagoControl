package com.devpaul.pagocontrol.features.home.domain.entity


data class PaymentSyncState(
    val ownerId: String? = null,
    val isSyncing: Boolean = false,
    val initialDownloadComplete: Boolean = false,
    val pendingWrites: Int = 0,
    val hasError: Boolean = false,
)

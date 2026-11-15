package com.devpaul.pagocontrol.features.home.data.datasource.remote.model

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity

internal data class PaymentCloudBatch(
    val baseRevision: Long,
    val revision: Long,
    val rows: List<NotificationEntity>,
)

package com.devpaul.pagocontrol.features.home.data.mapper

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification

internal fun NotificationEntity.asPaymentNotification() = PaymentNotification(
    id, packageName, source, title, text, postedAt, suggestedAmountCents,
)

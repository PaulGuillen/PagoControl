package com.devpaul.pagocontrol.features.home.domain.entity


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

package com.devpaul.pagocontrol.features.home.domain.entity


data class Movement(
    val id: String,
    val title: String,
    val detail: String,
    val source: String,
    val amountCents: Long,
    val automatic: Boolean = false,
)

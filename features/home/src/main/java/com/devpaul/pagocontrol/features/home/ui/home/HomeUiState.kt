package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.domain.PaymentNotification

enum class HomeSection { Wallets, Mail }

data class HomeUiState(
    val content: HomeContent = HomeContent.Loading,
    val monthly: Boolean = false,
    val selectedBar: Int = 5,
    val showUpcoming: Boolean = false,
    val signingOut: Boolean = false,
    val pendingNotifications: List<PaymentNotification> = emptyList(),
    val notificationSources: Set<String> = emptySet(),
    val notificationError: Boolean = false,
    val savingNotification: Boolean = false,
    val section: HomeSection = HomeSection.Wallets,
    val gmailContent: HomeContent = HomeContent.Loading,
    val gmailPendingNotifications: List<PaymentNotification> = emptyList(),
    val gmailMonitoring: Boolean = false,
    val gmailError: Boolean = false,
)

package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import com.devpaul.pagocontrol.features.home.ui.home.model.HomeContent
import com.devpaul.pagocontrol.features.home.ui.home.model.HomeSection
import com.devpaul.pagocontrol.features.home.ui.home.util.currentChartBar

data class HomeUiState(
    val content: HomeContent = HomeContent.Loading,
    val monthly: Boolean = false,
    val selectedBar: Int = currentChartBar(monthly = false),
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
    val guideStep: Int? = null,
)

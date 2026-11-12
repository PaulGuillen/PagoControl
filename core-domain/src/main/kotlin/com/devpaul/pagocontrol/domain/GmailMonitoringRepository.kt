package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

interface GmailMonitoringRepository {
    fun observeGmailDashboard(): Flow<DashboardResult>

    fun observeGmailPending(): Flow<List<PaymentNotification>>

    fun observeGmailEnabled(): Flow<Boolean>

    suspend fun setGmailEnabled(enabled: Boolean)
}

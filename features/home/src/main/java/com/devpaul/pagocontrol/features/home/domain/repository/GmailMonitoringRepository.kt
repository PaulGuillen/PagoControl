package com.devpaul.pagocontrol.features.home.domain.repository

import com.devpaul.pagocontrol.features.home.domain.entity.DashboardResult
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import kotlinx.coroutines.flow.Flow

interface GmailMonitoringRepository {
    fun observeGmailDashboard(): Flow<DashboardResult>

    fun observeGmailPending(): Flow<List<PaymentNotification>>

    fun observeGmailEnabled(): Flow<Boolean>

    suspend fun setGmailEnabled(enabled: Boolean)
}

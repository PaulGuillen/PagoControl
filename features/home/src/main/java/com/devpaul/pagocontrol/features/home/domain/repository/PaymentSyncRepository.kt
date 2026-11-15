package com.devpaul.pagocontrol.features.home.domain.repository

import com.devpaul.pagocontrol.features.home.domain.entity.PaymentSyncState
import kotlinx.coroutines.flow.StateFlow

interface PaymentSyncRepository {
    val state: StateFlow<PaymentSyncState>
    fun requestSync()
}

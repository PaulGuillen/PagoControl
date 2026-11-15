package com.devpaul.pagocontrol.features.home.domain.repository

import com.devpaul.pagocontrol.features.home.domain.entity.DashboardResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface HomeRepository {
    suspend fun loadDashboard(): DashboardResult

    fun observeDashboard(): Flow<DashboardResult> = flow { emit(loadDashboard()) }
}

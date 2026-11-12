package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class Movement(
    val id: String,
    val title: String,
    val detail: String,
    val source: String,
    val amountCents: Long,
    val automatic: Boolean = false,
)

data class Dashboard(
    val balanceCents: Long,
    val incomeCents: Long,
    val expenseCents: Long,
    val incomeCount: Int,
    val expenseCount: Int,
    val weeklyFlow: List<Long>,
    val monthlyFlow: List<Long>,
    val movements: List<Movement>,
    val weeklyIncomeFlow: List<Long> = weeklyFlow,
    val monthlyIncomeFlow: List<Long> = monthlyFlow,
    val receivedMovements: List<Movement> = movements.filter { it.amountCents > 0 },
)

sealed interface DashboardResult {
    data class Success(val dashboard: Dashboard) : DashboardResult
    data object SessionRequired : DashboardResult
    data object Unavailable : DashboardResult
}

interface HomeRepository {
    suspend fun loadDashboard(): DashboardResult

    fun observeDashboard(): Flow<DashboardResult> = flow { emit(loadDashboard()) }
}

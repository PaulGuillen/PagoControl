package com.devpaul.pagocontrol.features.home.domain.entity


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

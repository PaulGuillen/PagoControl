package com.devpaul.pagocontrol.data.local

import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.domain.HomeRepository
import com.devpaul.pagocontrol.domain.Movement
import com.devpaul.pagocontrol.session.AuthRepository

class DemoHomeRepository(private val authRepository: AuthRepository) : HomeRepository {
    override suspend fun loadDashboard(): DashboardResult {
        if (authRepository.session.value == null) return DashboardResult.SessionRequired
        return DashboardResult.Success(
            Dashboard(
                balanceCents = 458050,
                incomeCents = 685000,
                expenseCents = 226950,
                incomeCount = 18,
                expenseCount = 34,
                weeklyFlow = listOf(21000, 28000, 17500, 35000, 24500, 42000, 14000),
                monthlyFlow = listOf(140000, 185000, 160000, 200000),
                movements = listOf(
                    Movement("1", "María Rodríguez", "Hoy, 10:42 a. m. · Venta / Cobro", "Yape", 8500, true),
                    Movement("2", "Almuerzo La Lucha", "Ayer, 2:15 p. m. · Alimentación", "Plin", -3250),
                    Movement("3", "Netflix Perú", "28 nov · Suscripción", "Interbank", -4490),
                    Movement("4", "Cliente Consultoría", "27 nov, 4:50 p. m. · Servicios", "BCP", 45000),
                ),
            ),
        )
    }
}

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
                    Movement(id = "1", title = "María Rodríguez", detail = "Hoy, 10:42 a. m. · Venta / Cobro", source = "Yape", amountCents = 8500, automatic = true),
                    Movement(id = "2", title = "Almuerzo La Lucha", detail = "Ayer, 2:15 p. m. · Alimentación", source = "Plin", amountCents = -3250),
                    Movement(id = "3", title = "Netflix Perú", detail = "28 nov · Suscripción", source = "Interbank", amountCents = -4490),
                    Movement(id = "4", title = "Cliente Consultoría", detail = "27 nov, 4:50 p. m. · Servicios", source = "BCP", amountCents = 45000),
                ),
            ),
        )
    }
}

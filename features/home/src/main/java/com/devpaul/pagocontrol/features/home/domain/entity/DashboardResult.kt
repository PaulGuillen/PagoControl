package com.devpaul.pagocontrol.features.home.domain.entity


sealed interface DashboardResult {
    data class Success(val dashboard: Dashboard) : DashboardResult
    data object SessionRequired : DashboardResult
    data object Unavailable : DashboardResult
}

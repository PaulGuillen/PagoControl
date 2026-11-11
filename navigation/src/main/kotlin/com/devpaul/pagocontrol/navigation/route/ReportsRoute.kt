package com.devpaul.pagocontrol.navigation.route

import kotlinx.serialization.Serializable

sealed interface ReportsRoute : AppRoute {
    @Serializable
    data object Graph : ReportsRoute, GraphRoute
    @Serializable
    data object Reports : ReportsRoute, DestinationRoute
}

package com.devpaul.pagocontrol.navigation.route

import kotlinx.serialization.Serializable

sealed interface MovementsRoute : AppRoute {
    @Serializable
    data object Graph : MovementsRoute, GraphRoute

    @Serializable
    data object Movements : MovementsRoute, DestinationRoute
}

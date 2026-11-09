package com.devpaul.pagocontrol.navigation.route

import kotlinx.serialization.Serializable

sealed interface HomeRoute : AppRoute {
    @Serializable
    data object Graph : HomeRoute, GraphRoute

    @Serializable
    data object Home : HomeRoute, DestinationRoute
}

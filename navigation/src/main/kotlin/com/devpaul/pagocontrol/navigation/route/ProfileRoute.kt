package com.devpaul.pagocontrol.navigation.route

import kotlinx.serialization.Serializable

sealed interface ProfileRoute : AppRoute {
    @Serializable
    data object Graph : ProfileRoute, GraphRoute
    @Serializable
    data object Profile : ProfileRoute, DestinationRoute
    @Serializable
    data object Help : ProfileRoute, DestinationRoute
    @Serializable
    data object Privacy : ProfileRoute, DestinationRoute
}

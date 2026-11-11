package com.devpaul.navigation.route

import kotlinx.serialization.Serializable

sealed interface SplashRoute : AppRoute {

    @Serializable
    data object Graph : SplashRoute, GraphRoute

    @Serializable
    data object Splash : SplashRoute, DestinationRoute
}

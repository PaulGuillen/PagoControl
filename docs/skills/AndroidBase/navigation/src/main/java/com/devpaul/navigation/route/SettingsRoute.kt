package com.devpaul.navigation.route

import kotlinx.serialization.Serializable

sealed interface SettingsRoute : AppRoute {

    @Serializable
    data object Graph : SettingsRoute, GraphRoute

    @Serializable
    data object Settings : SettingsRoute, DestinationRoute

    @Serializable
    data object MyPlan : SettingsRoute, DestinationRoute

    @Serializable
    data object HelpCenter : SettingsRoute, DestinationRoute

    @Serializable
    data object Privacy : SettingsRoute, DestinationRoute
}

package com.devpaul.navigation.route

import kotlinx.serialization.Serializable

sealed interface HomeRoute : AppRoute {

    @Serializable
    data object Graph : HomeRoute, GraphRoute

    @Serializable
    data object Home : HomeRoute, DestinationRoute

    @Serializable
    data object Notifications : HomeRoute, DestinationRoute

    @Serializable
    data object Templates : HomeRoute, DestinationRoute

    @Serializable
    data class TemplatePreview(val templateId: String) : HomeRoute, DestinationRoute
}

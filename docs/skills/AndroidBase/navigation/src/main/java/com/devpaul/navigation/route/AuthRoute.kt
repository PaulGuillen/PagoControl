package com.devpaul.navigation.route

import kotlinx.serialization.Serializable

sealed interface AuthRoute : AppRoute {

    @Serializable
    data object Graph : AuthRoute, GraphRoute

    @Serializable
    data object Login : AuthRoute, DestinationRoute

    @Serializable
    data object Register : AuthRoute, DestinationRoute

    @Serializable
    data object ForgotPassword : AuthRoute, DestinationRoute

    @Serializable
    data class PasswordResetSent(val email: String) : AuthRoute, DestinationRoute
}

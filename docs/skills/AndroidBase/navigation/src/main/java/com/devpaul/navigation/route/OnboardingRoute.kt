package com.devpaul.navigation.route

import kotlinx.serialization.Serializable

sealed interface OnboardingRoute : AppRoute {

    @Serializable
    data object Graph : OnboardingRoute, GraphRoute

    @Serializable
    data object Welcome : OnboardingRoute, DestinationRoute
}

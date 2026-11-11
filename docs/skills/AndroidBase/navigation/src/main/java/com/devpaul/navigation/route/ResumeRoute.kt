package com.devpaul.navigation.route

import kotlinx.serialization.Serializable

sealed interface ResumeRoute : AppRoute {

    @Serializable
    data object Graph : ResumeRoute, GraphRoute

    @Serializable
    data object ResumeList : ResumeRoute, DestinationRoute

    @Serializable
    data class Importing(val uri: String, val useAi: Boolean = false) : ResumeRoute, DestinationRoute

    @Serializable
    data object SectionSelection : ResumeRoute, DestinationRoute

    @Serializable
    data object Contact : ResumeRoute, DestinationRoute

    @Serializable
    data object Experience : ResumeRoute, DestinationRoute

    @Serializable
    data object Education : ResumeRoute, DestinationRoute

    @Serializable
    data object Skills : ResumeRoute, DestinationRoute

    @Serializable
    data object Courses : ResumeRoute, DestinationRoute

    @Serializable
    data object Links : ResumeRoute, DestinationRoute

    @Serializable
    data object Languages : ResumeRoute, DestinationRoute

    @Serializable
    data object References : ResumeRoute, DestinationRoute

    @Serializable
    data object Summary : ResumeRoute, DestinationRoute

    @Serializable
    data object Template : ResumeRoute, DestinationRoute

    @Serializable
    data object Preview : ResumeRoute, DestinationRoute
}

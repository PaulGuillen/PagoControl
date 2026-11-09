package com.devpaul.pagocontrol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.devpaul.pagocontrol.session.UserSession
import com.devpaul.pagocontrol.navigation.AuthGraph
import com.devpaul.pagocontrol.navigation.HomeGraph
import com.devpaul.pagocontrol.features.auth.authGraph
import com.devpaul.pagocontrol.features.home.homeGraph

@Composable
fun MainGraph(session: UserSession?, themeIndex: Int, onThemeSelected: (Int) -> Unit) {
    val controller = rememberNavController()
    LaunchedEffect(session?.id) {
        val correctGraph = controller.currentDestination?.hierarchy?.any {
            if (session == null) it.hasRoute<AuthGraph>() else it.hasRoute<HomeGraph>()
        } == true
        if (!correctGraph) {
            controller.navigate(if (session == null) AuthGraph else HomeGraph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(controller, startDestination = AuthGraph) {
        authGraph(themeIndex, onThemeSelected)
        homeGraph(session?.name.orEmpty(), themeIndex, onThemeSelected, authorized = session != null)
    }
}

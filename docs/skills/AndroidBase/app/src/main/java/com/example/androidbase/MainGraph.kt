package com.example.androidbase

import com.example.androidbase.navigation.route.AuthRoute
import com.example.androidbase.navigation.route.HomeRoute
import com.example.androidbase.navigation.route.SplashRoute
import com.example.androidbase.navigation.route.OnboardingRoute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidbase.features.auth.authGraph
import com.example.androidbase.features.home.homeGraph
import com.example.androidbase.features.splash.splashGraph
import com.example.androidbase.features.onboarding.onboardingGraph
import com.example.androidbase.session.SessionState

@Composable
fun MainGraph(session: SessionState, darkTheme: Boolean, onSignOut: () -> Unit, onThemeSelected: (Int) -> Unit) {
    val user = (session as? SessionState.SignedIn)?.user
    val controller = rememberNavController()
    var onboardingCompleted by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val currentEntry by controller.currentBackStackEntryAsState()
    LaunchedEffect(user?.id, session is SessionState.Loading, currentEntry?.destination, onboardingCompleted) {
        if (session == SessionState.Loading) return@LaunchedEffect
        val destination = currentEntry?.destination ?: return@LaunchedEffect
        val splash = destination.hierarchy.any { it.hasRoute<SplashRoute.Graph>() }
        val onboarding = destination.hierarchy.any { it.hasRoute<OnboardingRoute.Graph>() }
        if (splash) {
            val completed = onboardingCompleted ?: return@LaunchedEffect
            controller.navigate(if (user != null) HomeRoute.Graph else if (completed) AuthRoute.Graph else OnboardingRoute.Graph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!onboarding) {
            val correct = destination.hierarchy.any { if (user == null) it.hasRoute<AuthRoute.Graph>() else it.hasRoute<HomeRoute.Graph>() }
            if (!correct) controller.navigate(if (user == null) AuthRoute.Graph else HomeRoute.Graph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(navController = controller, startDestination = SplashRoute.Graph) {
        splashGraph { onboardingCompleted = it }
        onboardingGraph {
            controller.navigate(AuthRoute.Graph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        authGraph(
            themeIndex = if (darkTheme) 2 else 1, onThemeSelected = onThemeSelected,
            onRegister = { controller.navigate(AuthRoute.Register) { launchSingleTop = true } },
            onForgotPassword = { controller.navigate(AuthRoute.ForgotPassword) { launchSingleTop = true } },
            onLogin = { controller.popBackStack<AuthRoute.Login>(inclusive = false) },
        )
        if (user != null) homeGraph(user = user, darkTheme = darkTheme, onSignOut = onSignOut)
        else navigation<HomeRoute.Graph>(startDestination = HomeRoute.Home) {
            composable<HomeRoute.Home> { }
        }
    }
}

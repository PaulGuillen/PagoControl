package com.devpaul.pagocontrol

import com.devpaul.pagocontrol.navigation.route.AuthRoute
import com.devpaul.pagocontrol.navigation.route.HomeRoute
import com.devpaul.pagocontrol.navigation.route.MovementsRoute
import com.devpaul.pagocontrol.navigation.route.ProfileRoute
import com.devpaul.pagocontrol.navigation.route.GraphRoute
import com.devpaul.pagocontrol.features.profile.profileGraph
import com.devpaul.pagocontrol.features.movements.movementsGraph
import com.devpaul.pagocontrol.navigation.route.SplashRoute
import com.devpaul.pagocontrol.navigation.route.OnboardingRoute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devpaul.pagocontrol.session.UserSession
import com.devpaul.pagocontrol.features.auth.authGraph
import com.devpaul.pagocontrol.features.home.homeGraph
import com.devpaul.pagocontrol.features.splash.splashGraph
import com.devpaul.pagocontrol.features.onboarding.onboardingGraph

@Composable
fun MainGraph(session: UserSession?, themeIndex: Int, onThemeSelected: (Int) -> Unit) {
    val controller = rememberNavController()
    val context = LocalContext.current
    val version = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    fun navigateTab(route: GraphRoute) {
        controller.navigate(route) {
            popUpTo<HomeRoute.Home> { saveState = true }
            restoreState = true
            launchSingleTop = true
        }
    }
    var onboardingCompleted by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val currentEntry by controller.currentBackStackEntryAsState()
    LaunchedEffect(key1 = session?.id, key2 = currentEntry?.destination, key3 = onboardingCompleted) {
        val destination = currentEntry?.destination ?: return@LaunchedEffect
        val splash = destination.hierarchy.any { it.hasRoute<SplashRoute.Graph>() }
        val onboarding = destination.hierarchy.any { it.hasRoute<OnboardingRoute.Graph>() }
        if (splash) {
            val completed = onboardingCompleted ?: return@LaunchedEffect
            controller.navigate(if (session != null) HomeRoute.Graph else if (completed) AuthRoute.Graph else OnboardingRoute.Graph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!onboarding) {
            val correct = destination.hierarchy.any { if (session == null) it.hasRoute<AuthRoute.Graph>() else it.hasRoute<HomeRoute.Graph>() || it.hasRoute<MovementsRoute.Graph>() || it.hasRoute<ProfileRoute.Graph>() }
            if (!correct) {
                controller.clearBackStack<MovementsRoute.Graph>()
                controller.clearBackStack<ProfileRoute.Graph>()
                controller.navigate(if (session == null) AuthRoute.Graph else HomeRoute.Graph) {
                    popUpTo(controller.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
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
            themeIndex = themeIndex, onThemeSelected = onThemeSelected,
            onRegister = { controller.navigate(AuthRoute.Register) { launchSingleTop = true } },
            onForgotPassword = { controller.navigate(AuthRoute.ForgotPassword) { launchSingleTop = true } },
            onLogin = { controller.popBackStack<AuthRoute.Login>(inclusive = false) },
        )
        homeGraph(name = session?.name.orEmpty(), themeIndex = themeIndex, onThemeSelected = onThemeSelected, authorized = session != null,
            onMovements = { navigateTab(MovementsRoute.Graph) },
            onProfile = { navigateTab(ProfileRoute.Graph) },
        )
        movementsGraph(
            authorized = session != null,
            onHome = { controller.popBackStack<HomeRoute.Home>(inclusive = false) },
            onProfile = { navigateTab(ProfileRoute.Graph) },
        )
        profileGraph(
            authorized = session != null, themeIndex = themeIndex, version = version, onThemeSelected = onThemeSelected,
            onHome = { controller.popBackStack<HomeRoute.Home>(inclusive = false) },
            onMovements = { navigateTab(MovementsRoute.Graph) },
            onHelp = { controller.navigate(ProfileRoute.Help) { launchSingleTop = true } },
            onPrivacy = { controller.navigate(ProfileRoute.Privacy) { launchSingleTop = true } },
            onBack = { controller.popBackStack() },
            onReports = {},
        )
    }
}

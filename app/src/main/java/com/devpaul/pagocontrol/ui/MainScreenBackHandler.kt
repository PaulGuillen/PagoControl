package com.devpaul.pagocontrol.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.devpaul.pagocontrol.navigation.route.HomeRoute
import com.devpaul.pagocontrol.navigation.route.MovementsRoute
import com.devpaul.pagocontrol.navigation.route.ReportsRoute
import com.devpaul.pagocontrol.navigation.route.ProfileRoute

@Composable
internal fun MainScreenBackHandler(
    destination: NavDestination?,
    onRequestLogout: () -> Unit,
) {
    val isMainScreen = destination != null && (
        destination.hasRoute<HomeRoute.Home>() ||
            destination.hasRoute<MovementsRoute.Movements>() ||
            destination.hasRoute<ReportsRoute.Reports>() ||
            destination.hasRoute<ProfileRoute.Profile>()
        )
    BackHandler(enabled = isMainScreen, onBack = onRequestLogout)
}

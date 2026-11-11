package com.devpaul.cvfree.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.devpaul.navigation.route.HomeRoute
import com.devpaul.navigation.route.ResumeRoute
import com.devpaul.navigation.route.SettingsRoute

@Composable
internal fun MainScreenBackHandler(
    destination: NavDestination?,
    onRequestLogout: () -> Unit,
) {
    val isMainScreen = destination != null && (
        destination.hasRoute<HomeRoute.Home>() ||
            destination.hasRoute<ResumeRoute.ResumeList>() ||
            destination.hasRoute<SettingsRoute.Settings>()
        )
    BackHandler(enabled = isMainScreen, onBack = onRequestLogout)
}

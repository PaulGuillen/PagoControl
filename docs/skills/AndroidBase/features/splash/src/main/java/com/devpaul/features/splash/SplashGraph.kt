package com.devpaul.features.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.features.splash.ui.SplashScreen
import com.devpaul.navigation.route.SplashRoute

fun NavGraphBuilder.splashGraph(
    onFinished: () -> Unit,
    isPremium: Boolean = false,
    versionName: String = "",
) {
    navigation<SplashRoute.Graph>(
        startDestination = SplashRoute.Splash,
    ) {
        composable<SplashRoute.Splash> {
            SplashScreen(onFinished = onFinished, isPremium = isPremium, versionName = versionName)
        }
    }
}

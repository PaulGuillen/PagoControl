package com.devpaul.pagocontrol.features.splash

import com.devpaul.pagocontrol.navigation.route.SplashRoute

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.features.splash.ui.splash.SplashScreen
import com.devpaul.pagocontrol.features.splash.ui.splash.SplashViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.splashGraph(onReady: (Boolean) -> Unit) {
    navigation<SplashRoute.Graph>(startDestination = SplashRoute.Splash) {
        composable<SplashRoute.Splash> {
            val viewModel = koinViewModel<SplashViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(state.onboardingCompleted) { state.onboardingCompleted?.let(onReady) }
            SplashScreen(state, viewModel::onIntent)
        }
    }
}

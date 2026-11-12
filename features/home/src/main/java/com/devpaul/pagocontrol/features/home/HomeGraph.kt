package com.devpaul.pagocontrol.features.home

import com.devpaul.pagocontrol.navigation.route.HomeRoute

import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(name: String, themeIndex: Int, onThemeSelected: (Int) -> Unit, authorized: Boolean, onMovements: () -> Unit = {}, onProfile: () -> Unit = {}, onReports: () -> Unit = {}) {
    navigation<HomeRoute.Graph>(startDestination = HomeRoute.Home) {
        composable<HomeRoute.Home> {
            if (authorized) {
                val viewModel = koinViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                LaunchedEffect(viewModel, lifecycle) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        while (true) {
                            viewModel.onIntent(HomeUiIntent.RefreshDate)
                            delay(60_000)
                        }
                    }
                }
                HomeScreen(
                    state = state,
                    name = name,
                    onIntent = viewModel::onIntent,
                    themeIndex = themeIndex,
                    onThemeSelected = onThemeSelected,
                    onMovements = onMovements,
                    onProfile = onProfile,
                    onReports = onReports,
                )
            }
        }
    }
}

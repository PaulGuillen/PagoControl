package com.devpaul.pagocontrol.features.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.features.home.domain.repository.PaymentSyncRepository
import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import com.devpaul.pagocontrol.features.home.ui.home.HomeViewModel
import com.devpaul.pagocontrol.navigation.route.HomeRoute
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.homeGraph(
    name: String,
    themeIndex: Int,
    onThemeSelected: (Int) -> Unit,
    authorized: Boolean,
    onMovements: () -> Unit = {},
    onProfile: () -> Unit = {},
    onReports: () -> Unit = {}
) {
    navigation<HomeRoute.Graph>(startDestination = HomeRoute.Home) {
        composable<HomeRoute.Home> {
            if (authorized) {
                val viewModel = koinViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val sync = koinInject<PaymentSyncRepository>()
                val syncState by sync.state.collectAsStateWithLifecycle()
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
                    syncState = syncState,
                    onSync = sync::requestSync,
                )
            }
        }
    }
}

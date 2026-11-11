package com.devpaul.pagocontrol.features.movements

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.features.movements.ui.movements.MovementsScreen
import com.devpaul.pagocontrol.features.movements.ui.movements.MovementsViewModel
import com.devpaul.pagocontrol.navigation.route.MovementsRoute
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.movementsGraph(authorized: Boolean, onHome: () -> Unit, onProfile: () -> Unit = {}, onReports: () -> Unit = {}) {
    navigation<MovementsRoute.Graph>(startDestination = MovementsRoute.Movements) {
        composable<MovementsRoute.Movements> {
            if (authorized) {
                val viewModel = koinViewModel<MovementsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                MovementsScreen(state, viewModel::onIntent, onHome, onProfile, onReports)
            }
        }
    }
}

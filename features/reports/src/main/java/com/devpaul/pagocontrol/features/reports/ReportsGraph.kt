package com.devpaul.pagocontrol.features.reports

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportsScreen
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportsViewModel
import com.devpaul.pagocontrol.navigation.route.ReportsRoute
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.reportsGraph(authorized: Boolean, onHome: () -> Unit, onMovements: () -> Unit, onProfile: () -> Unit) {
    navigation<ReportsRoute.Graph>(startDestination = ReportsRoute.Reports) {
        composable<ReportsRoute.Reports> {
            if (authorized) {
                val viewModel = koinViewModel<ReportsViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ReportsScreen(state, viewModel::onIntent, onHome, onMovements, onProfile)
            }
        }
    }
}

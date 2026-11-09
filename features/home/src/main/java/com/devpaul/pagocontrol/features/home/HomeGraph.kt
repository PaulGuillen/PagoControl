package com.devpaul.pagocontrol.features.home

import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeViewModel

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.navigation.HomeGraph
import com.devpaul.pagocontrol.navigation.Home
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(name: String, themeIndex: Int, onThemeSelected: (Int) -> Unit, authorized: Boolean) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            if (authorized) {
                val viewModel = koinViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                HomeScreen(state, name, viewModel::onIntent, themeIndex, onThemeSelected)
            }
        }
    }
}

package com.devpaul.pagocontrol.features.auth

import com.devpaul.pagocontrol.features.auth.ui.login.LoginScreen
import com.devpaul.pagocontrol.features.auth.ui.login.LoginViewModel

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.navigation.AuthGraph
import com.devpaul.pagocontrol.navigation.Login
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.authGraph(themeIndex: Int, onThemeSelected: (Int) -> Unit) {
    navigation<AuthGraph>(startDestination = Login) {
        composable<Login> {
            val viewModel = koinViewModel<LoginViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LoginScreen(state, viewModel::onIntent, themeIndex, onThemeSelected)
        }
    }
}

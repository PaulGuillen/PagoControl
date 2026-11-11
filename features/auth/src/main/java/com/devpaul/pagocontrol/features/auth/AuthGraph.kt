package com.devpaul.pagocontrol.features.auth

import com.devpaul.pagocontrol.navigation.route.AuthRoute

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.features.auth.ui.login.LoginScreen
import com.devpaul.pagocontrol.features.auth.ui.login.LoginViewModel
import com.devpaul.pagocontrol.features.auth.ui.login.LoginUiIntent
import com.devpaul.pagocontrol.features.auth.ui.register.RegisterScreen
import com.devpaul.pagocontrol.features.auth.ui.register.RegisterViewModel
import com.devpaul.pagocontrol.features.auth.ui.register.RegisterUiIntent
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.ForgotPasswordScreen
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.ForgotPasswordViewModel
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.ForgotPasswordUiIntent
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.authGraph(
    themeIndex: Int, onThemeSelected: (Int) -> Unit,
    onRegister: () -> Unit, onForgotPassword: () -> Unit, onLogin: () -> Unit,
) {
    navigation<AuthRoute.Graph>(startDestination = AuthRoute.Login) {
        composable<AuthRoute.Login> {
            val viewModel = koinViewModel<LoginViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LoginScreen(
                state = state,
                onIntent = viewModel::onIntent,
                themeIndex = themeIndex,
                onThemeSelected = onThemeSelected,
                onRegister = { viewModel.onIntent(LoginUiIntent.ClearError); onRegister() },
                onForgotPassword = { viewModel.onIntent(LoginUiIntent.ClearError); onForgotPassword() },
            )
        }
        composable<AuthRoute.Register> {
            val viewModel = koinViewModel<RegisterViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            RegisterScreen(
                state = state,
                onIntent = viewModel::onIntent,
                onLogin = { viewModel.onIntent(RegisterUiIntent.ClearError); onLogin() },
                themeIndex = themeIndex,
                onThemeSelected = onThemeSelected,
            )
        }
        composable<AuthRoute.ForgotPassword> {
            val viewModel = koinViewModel<ForgotPasswordViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ForgotPasswordScreen(
                state = state,
                onIntent = viewModel::onIntent,
                onLogin = { viewModel.onIntent(ForgotPasswordUiIntent.ClearError); onLogin() },
            )
        }
    }
}

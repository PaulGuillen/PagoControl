package com.devpaul.features.auth

import androidx.compose.ui.platform.LocalResources
import com.devpaul.core.platform.locale.AppLanguage

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.devpaul.core.designsystem.component.mvi.BaseScreenWithState
import com.devpaul.features.auth.ui.forgot.ForgotPasswordScreen
import com.devpaul.features.auth.ui.forgot.ForgotPasswordUiEvent
import com.devpaul.features.auth.ui.forgot.ForgotPasswordUiIntent
import com.devpaul.features.auth.ui.forgot.ForgotPasswordViewModel
import com.devpaul.features.auth.ui.forgot.PasswordResetSentScreen
import com.devpaul.features.auth.ui.login.LoginScreen
import com.devpaul.features.auth.ui.login.LoginUiEvent
import com.devpaul.features.auth.ui.login.LoginUiIntent
import com.devpaul.features.auth.ui.login.LoginViewModel
import com.devpaul.features.auth.ui.register.RegisterScreen
import com.devpaul.features.auth.ui.register.RegisterUiEvent
import com.devpaul.features.auth.ui.register.RegisterUiIntent
import com.devpaul.features.auth.ui.register.RegisterViewModel
import com.devpaul.navigation.route.AuthRoute
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.authGraph(
    onAuthenticated: () -> Unit,
    onForgotPassword: () -> Unit,
    onPasswordResetSent: (email: String) -> Unit,
    onBackFromForgotPassword: () -> Unit,
    onBackToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    isPremium: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.Spanish,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    versionName: String = "",
) {
    navigation<AuthRoute.Graph>(
        startDestination = AuthRoute.Login,
    ) {
        composable<AuthRoute.Login> {
            val loginViewModel: LoginViewModel = koinViewModel()
            val resources = LocalResources.current
            BaseScreenWithState(
                viewModel = loginViewModel,
                onUiEvent = { event, showSnackbar ->
                    when (event) {
                        LoginUiEvent.Authenticated -> onAuthenticated()
                        is LoginUiEvent.ShowSnackbar -> showSnackbar(resources.getString(event.message))
                    }
                },
            ) { uiState, onIntent ->
                LoginScreen(
                    versionName = versionName,
                    isPremium = isPremium,
                    appLanguage = appLanguage,
                    onLanguageSelected = onLanguageSelected,
                    onLogin = { email, password ->
                        onIntent(LoginUiIntent.Login(email, password))
                    },
                    onForgotPassword = {
                        onIntent(LoginUiIntent.ClearError)
                        onForgotPassword()
                    },
                    onNavigateToRegister = {
                        onIntent(LoginUiIntent.ClearError)
                        onNavigateToRegister()
                    },
                    uiState = uiState,
                    onInputChanged = { onIntent(LoginUiIntent.ClearError) },
                )
            }
        }
        composable<AuthRoute.Register> {
            val registerViewModel: RegisterViewModel = koinViewModel()
            val resources = LocalResources.current
            BaseScreenWithState(
                viewModel = registerViewModel,
                onUiEvent = { event, showSnackbar ->
                    when (event) {
                        RegisterUiEvent.Authenticated -> onAuthenticated()
                        is RegisterUiEvent.ShowSnackbar -> showSnackbar(resources.getString(event.message))
                    }
                },
            ) { uiState, onIntent ->
                RegisterScreen(
                    isPremium = isPremium,
                    appLanguage = appLanguage,
                    onLanguageSelected = onLanguageSelected,
                    onRegister = { name, email, password ->
                        onIntent(RegisterUiIntent.Register(name, email, password))
                    },
                    onNavigateToLogin = {
                        onIntent(RegisterUiIntent.ClearError)
                        onNavigateToLogin()
                    },
                    uiState = uiState,
                    onInputChanged = { onIntent(RegisterUiIntent.ClearError) },
                )
            }
        }
        composable<AuthRoute.ForgotPassword> {
            val forgotPasswordViewModel: ForgotPasswordViewModel = koinViewModel()
            val resources = LocalResources.current
            BaseScreenWithState(
                viewModel = forgotPasswordViewModel,
                onUiEvent = { event, showSnackbar ->
                    when (event) {
                        is ForgotPasswordUiEvent.PasswordResetSent ->
                            onPasswordResetSent(event.email)
                        is ForgotPasswordUiEvent.ShowSnackbar -> showSnackbar(resources.getString(event.message))
                    }
                },
            ) { uiState, onIntent ->
                ForgotPasswordScreen(
                    isPremium = isPremium,
                    appLanguage = appLanguage,
                    onLanguageSelected = onLanguageSelected,
                    uiState = uiState,
                    onSend = { email -> onIntent(ForgotPasswordUiIntent.Send(email)) },
                    onInputChanged = { onIntent(ForgotPasswordUiIntent.ClearError) },
                    onNavigateBack = {
                        onIntent(ForgotPasswordUiIntent.ClearError)
                        onBackFromForgotPassword()
                    },
                )
            }
        }
        composable<AuthRoute.PasswordResetSent> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthRoute.PasswordResetSent>()
            PasswordResetSentScreen(
                email = route.email,
                onBackToLogin = onBackToLogin,
            )
        }
    }
}

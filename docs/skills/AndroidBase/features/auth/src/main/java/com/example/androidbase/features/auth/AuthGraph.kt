package com.example.androidbase.features.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.androidbase.features.auth.ui.login.LoginScreen
import com.example.androidbase.navigation.AuthRoute

fun NavGraphBuilder.authGraph() {
    composable<AuthRoute> { LoginScreen() }
}

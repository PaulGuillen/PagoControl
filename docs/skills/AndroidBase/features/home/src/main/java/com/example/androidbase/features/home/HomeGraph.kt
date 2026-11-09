package com.example.androidbase.features.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.androidbase.features.home.ui.home.HomeScreen
import com.example.androidbase.navigation.HomeRoute
import com.example.androidbase.session.SessionUser

fun NavGraphBuilder.homeGraph(user: SessionUser, darkTheme: Boolean, onSignOut: () -> Unit) {
    composable<HomeRoute> { HomeScreen(user, darkTheme, onSignOut) }
}

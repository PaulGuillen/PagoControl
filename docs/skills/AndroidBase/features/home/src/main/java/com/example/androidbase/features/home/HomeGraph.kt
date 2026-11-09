package com.example.androidbase.features.home

import com.example.androidbase.navigation.route.HomeRoute

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.androidbase.features.home.ui.home.HomeScreen
import com.example.androidbase.session.SessionUser

fun NavGraphBuilder.homeGraph(user: SessionUser, darkTheme: Boolean, onSignOut: () -> Unit) {
    navigation<HomeRoute.Graph>(startDestination = HomeRoute.Home) {
        composable<HomeRoute.Home> { HomeScreen(user = user, darkTheme = darkTheme, onSignOut = onSignOut) }
    }
}

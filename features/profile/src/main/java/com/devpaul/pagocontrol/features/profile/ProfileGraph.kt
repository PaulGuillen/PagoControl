package com.devpaul.pagocontrol.features.profile

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.pagocontrol.features.profile.ui.profile.ProfileScreen
import com.devpaul.pagocontrol.features.profile.ui.profile.ProfileViewModel
import com.devpaul.pagocontrol.features.profile.ui.support.ProfileSupportScreen
import com.devpaul.pagocontrol.navigation.route.ProfileRoute
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.profileGraph(
    authorized: Boolean,
    themeIndex: Int,
    version: String,
    onThemeSelected: (Int) -> Unit,
    onHome: () -> Unit,
    onMovements: () -> Unit,
    onHelp: () -> Unit,
    onPrivacy: () -> Unit,
    onBack: () -> Unit,
    onReports: () -> Unit,
) {
    navigation<ProfileRoute.Graph>(startDestination = ProfileRoute.Profile) {
        composable<ProfileRoute.Profile> {
            if (authorized) {
                val viewModel = koinViewModel<ProfileViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ProfileScreen(state, themeIndex, version, onThemeSelected, viewModel::onIntent, onHome, onMovements, onHelp, onPrivacy, onReports)
            }
        }
        composable<ProfileRoute.Help> { if (authorized) ProfileSupportScreen(privacy = false, onBack = onBack) }
        composable<ProfileRoute.Privacy> { if (authorized) ProfileSupportScreen(privacy = true, onBack = onBack) }
    }
}

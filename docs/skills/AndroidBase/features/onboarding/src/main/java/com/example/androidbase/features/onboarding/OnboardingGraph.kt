package com.example.androidbase.features.onboarding

import com.example.androidbase.navigation.route.OnboardingRoute

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.androidbase.features.onboarding.ui.onboarding.OnboardingScreen
import com.example.androidbase.features.onboarding.ui.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.onboardingGraph(onFinished: () -> Unit) {
    navigation<OnboardingRoute.Graph>(startDestination = OnboardingRoute.Welcome) {
        composable<OnboardingRoute.Welcome> {
            val viewModel = koinViewModel<OnboardingViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(state.destination) {
                state.destination?.let { onFinished() }
            }
            OnboardingScreen(state, viewModel::onIntent)
        }
    }
}

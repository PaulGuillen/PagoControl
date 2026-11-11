package com.devpaul.features.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.features.onboarding.ui.OnboardingScreen
import com.devpaul.navigation.route.OnboardingRoute

fun NavGraphBuilder.onboardingGraph(onFinished: () -> Unit) {
    navigation<OnboardingRoute.Graph>(
        startDestination = OnboardingRoute.Welcome,
    ) {
        composable<OnboardingRoute.Welcome> {
            OnboardingScreen(onFinished = onFinished)
        }
    }
}

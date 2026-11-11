package com.devpaul.core.platform.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun TrackAnalyticsView(component: String) {
    LaunchedEffect(component) {
        AppAnalytics.componentView(component)
    }
}

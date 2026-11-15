package com.devpaul.pagocontrol.features.home.domain.repository

import kotlinx.coroutines.flow.Flow

interface HomeGuidePreferences {
    val completedGuides: Flow<Set<String>>

    suspend fun completeGuide(section: String)
}

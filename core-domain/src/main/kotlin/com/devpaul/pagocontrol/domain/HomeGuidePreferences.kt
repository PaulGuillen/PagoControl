package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

interface HomeGuidePreferences {
    val completedGuides: Flow<Set<String>>

    suspend fun completeGuide(section: String)
}

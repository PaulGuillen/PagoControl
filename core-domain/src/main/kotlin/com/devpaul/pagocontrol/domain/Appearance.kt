package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

enum class ThemeMode { System, Light, Dark }

interface AppearanceRepository {
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}

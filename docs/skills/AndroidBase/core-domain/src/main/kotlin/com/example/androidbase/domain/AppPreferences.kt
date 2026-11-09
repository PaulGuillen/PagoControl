package com.example.androidbase.domain

import kotlinx.coroutines.flow.Flow

interface AppPreferences {
    val darkTheme: Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
}

package com.devpaul.pagocontrol.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.devpaul.pagocontrol.domain.StartupPreferences
import kotlinx.coroutines.flow.map

private val Context.startupStore by preferencesDataStore(name = "startup")

class DataStoreStartupPreferences(context: Context) : StartupPreferences {
    private val store = context.applicationContext.startupStore
    private val completedKey = booleanPreferencesKey("onboarding_completed")
    override val onboardingCompleted = store.data.map { it[completedKey] ?: false }

    override suspend fun completeOnboarding() { store.edit { it[completedKey] = true } }
}

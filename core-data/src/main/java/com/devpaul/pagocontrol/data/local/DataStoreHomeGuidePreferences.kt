package com.devpaul.pagocontrol.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devpaul.pagocontrol.domain.HomeGuidePreferences
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.homeGuideStore by preferencesDataStore(name = "home_guides")

class DataStoreHomeGuidePreferences(context: Context) : HomeGuidePreferences {
    private val store = context.applicationContext.homeGuideStore
    private val completedKey = stringSetPreferencesKey("completed_sections_v1")

    override val completedGuides = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { it[completedKey].orEmpty() }

    override suspend fun completeGuide(section: String) {
        store.edit { it[completedKey] = it[completedKey].orEmpty() + section }
    }
}

package com.example.androidbase.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidbase.domain.AppPreferences
import com.example.androidbase.domain.AppResult
import com.example.androidbase.domain.ExampleItem
import com.example.androidbase.domain.ExampleRepository
import com.example.androidbase.data.remote.dataCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.baseStore by preferencesDataStore(name = "android_base")

class DataStorePreferences(context: Context) : AppPreferences {
    private val store = context.applicationContext.baseStore
    private val darkKey = booleanPreferencesKey("dark_theme")
    override val darkTheme: Flow<Boolean> = store.data.map { it[darkKey] ?: false }

    override suspend fun setDarkTheme(enabled: Boolean) {
        store.edit { it[darkKey] = enabled }
    }
}

@Serializable
private data class StoredItem(val id: String, val title: String)

class LocalExampleRepository(context: Context) : ExampleRepository {
    private val store = context.applicationContext.baseStore
    private val json = Json { ignoreUnknownKeys = true }

    override fun observe(ownerId: String): Flow<List<ExampleItem>> = store.data.map { values ->
        decode(values[stringPreferencesKey("items_$ownerId")]).map { ExampleItem(it.id, it.title) }
    }

    override suspend fun add(ownerId: String, title: String): AppResult<Unit> = dataCall {
        store.edit { values ->
            val key = stringPreferencesKey("items_$ownerId")
            val items = decode(values[key]) + StoredItem(UUID.randomUUID().toString(), title)
            values[key] = json.encodeToString(items)
        }
        Unit
    }

    private fun decode(value: String?): List<StoredItem> =
        value?.let { json.decodeFromString<List<StoredItem>>(it) } ?: emptyList()
}

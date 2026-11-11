package com.devpaul.pagocontrol.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devpaul.pagocontrol.domain.ProfilePreferences
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.domain.LoginValidation
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "profile")

class DataStoreProfileRepository(context: Context) : ProfileRepository {
    private val store = context.applicationContext.profileDataStore

    override fun observe(userId: String) = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { preferences ->
        ProfilePreferences(
            name = preferences[stringPreferencesKey("$userId.name")],
            email = preferences[stringPreferencesKey("$userId.email")],
            notificationsEnabled = preferences[booleanPreferencesKey("$userId.notifications")] ?: true,
            rating = preferences[intPreferencesKey("$userId.rating")] ?: 0,
            ratingComment = preferences[stringPreferencesKey("$userId.rating_comment")] ?: "",
        )
    }

    override suspend fun saveIdentity(userId: String, name: String, email: String) {
        require(name.trim().isNotEmpty() && name.trim().length <= 80)
        require(email.isBlank() || LoginValidation.validEmail(email))
        store.edit {
            it[stringPreferencesKey("$userId.name")] = name.trim()
            it[stringPreferencesKey("$userId.email")] = email.trim()
        }
    }

    override suspend fun setNotifications(userId: String, enabled: Boolean) {
        store.edit { it[booleanPreferencesKey("$userId.notifications")] = enabled }
    }

    override suspend fun saveRating(userId: String, stars: Int, comment: String) {
        require(stars in 1..5 && comment.length <= 500)
        store.edit {
            it[intPreferencesKey("$userId.rating")] = stars
            it[stringPreferencesKey("$userId.rating_comment")] = comment.trim()
        }
    }
}

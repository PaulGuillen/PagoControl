package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

data class ProfilePreferences(
    val name: String? = null,
    val email: String? = null,
    val notificationsEnabled: Boolean = true,
    val rating: Int = 0,
    val ratingComment: String = "",
)

interface ProfileRepository {
    fun observe(userId: String): Flow<ProfilePreferences>

    suspend fun saveIdentity(userId: String, name: String, email: String)

    suspend fun setNotifications(userId: String, enabled: Boolean)

    suspend fun saveRating(userId: String, stars: Int, comment: String)
}

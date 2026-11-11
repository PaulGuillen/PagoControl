package com.devpaul.features.auth.data.security

import android.content.Context
import org.koin.core.annotation.Single

@Single([AuthAttemptStore::class])
class SharedPreferencesAuthAttemptStore(context: Context) : AuthAttemptStore {
    private val preferences = context.getSharedPreferences("auth_attempt_limits", Context.MODE_PRIVATE)

    override fun read(action: AuthAction): List<Long> = preferences.getString(action.name, null)
        ?.split(',')?.mapNotNull(String::toLongOrNull).orEmpty()

    override fun write(action: AuthAction, attempts: List<Long>) {
        check(preferences.edit().putString(action.name, attempts.joinToString(",")).commit()) {
            "Could not persist authentication attempt limits"
        }
    }
}

package com.devpaul.cvfree.notifications

import android.content.SharedPreferences
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.core.content.edit
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class SavedResumeNotification(val id: String, val savedAt: Long)

class NotificationHistoryStore(private val preferences: SharedPreferences) {
    private val mutableEntries = MutableStateFlow(readEntries())
    val entries = mutableEntries.asStateFlow()

    @Synchronized
    fun recordResumeSaved(savedAt: Long = System.currentTimeMillis()) {
        val updated = (listOf(SavedResumeNotification(UUID.randomUUID().toString(), savedAt)) +
            mutableEntries.value).sortedByDescending(SavedResumeNotification::savedAt)
        val serialized = JSONArray()
        updated.forEach { entry ->
            serialized.put(JSONObject().put("id", entry.id).put("savedAt", entry.savedAt))
        }
        preferences.edit { putString("entries", serialized.toString()) }
        mutableEntries.value = updated
    }

    private fun readEntries(): List<SavedResumeNotification> = try {
        val serialized = JSONArray(preferences.getString("entries", "[]"))
        buildList {
            for (index in 0 until serialized.length()) {
                val entry = serialized.optJSONObject(index) ?: continue
                val id = entry.optString("id")
                val savedAt = entry.optLong("savedAt", -1)
                if (id.isNotBlank() && savedAt >= 0) add(SavedResumeNotification(id, savedAt))
            }
        }.sortedByDescending(SavedResumeNotification::savedAt)
    } catch (error: JSONException) {
        AppAnalytics.error("load_notification_history", error)
        emptyList()
    }
}

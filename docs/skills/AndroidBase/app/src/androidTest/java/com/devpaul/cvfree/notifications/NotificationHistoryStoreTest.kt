package com.devpaul.cvfree.notifications

import android.content.Context
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHistoryStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences("notification_history_test", Context.MODE_PRIVATE)

    @Before
    fun prepare() {
        preferences.edit(commit = true) { clear() }
    }

    @After
    fun cleanUp() {
        preferences.edit(commit = true) { clear() }
    }

    @Test
    fun savesSurviveRecreationAndAreOrderedNewestFirst() {
        val store = NotificationHistoryStore(preferences)
        store.recordResumeSaved(100)
        store.recordResumeSaved(300)
        store.recordResumeSaved(200)

        val restored = NotificationHistoryStore(preferences).entries.value
        assertEquals(listOf(300L, 200L, 100L), restored.map(SavedResumeNotification::savedAt))
        assertEquals(store.entries.value, restored)
    }

    @Test
    fun savesAtTheSameTimeRemainSeparateEvents() {
        val store = NotificationHistoryStore(preferences)
        store.recordResumeSaved(100)
        store.recordResumeSaved(100)

        assertEquals(2, store.entries.value.size)
        assertEquals(2, store.entries.value.map(SavedResumeNotification::id).distinct().size)
    }

    @Test
    fun malformedHistoryDoesNotPreventFutureSaves() {
        preferences.edit { putString("entries", "invalid") }
        val store = NotificationHistoryStore(preferences)
        assertTrue(store.entries.value.isEmpty())

        store.recordResumeSaved(100)
        assertEquals(1, NotificationHistoryStore(preferences).entries.value.size)
    }
}

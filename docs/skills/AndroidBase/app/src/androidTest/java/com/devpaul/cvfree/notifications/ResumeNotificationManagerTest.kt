package com.devpaul.cvfree.notifications

import android.app.NotificationManager
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.core.platform.locale.AppLanguageManager
import com.devpaul.cvfree.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumeNotificationManagerTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val manager = ResumeNotificationManager(context)
    private val systemNotifications = context.getSystemService(NotificationManager::class.java)
    private var previouslyEnabled = true

    @Before
    fun prepare() {
        previouslyEnabled = manager.enabled
        manager.ensureChannel()
        manager.enabled = false
    }

    @After
    fun restore() {
        manager.enabled = false
        manager.enabled = previouslyEnabled
    }

    @Test
    fun disabledPreferenceSurvivesNewManagerAndSuppressesNotifications() {
        val restoredManager = ResumeNotificationManager(context)
        assertFalse(restoredManager.enabled)
        restoredManager.showResumeSaved()
        assertTrue(awaitSavedNotifications(0).isEmpty())
    }

    @Test
    fun savedNotificationUsesSelectedLanguageAndIncludesAppIntent() {
        assumeTrue(manager.canPostNotifications())
        manager.enabled = true
        manager.showResumeSaved()

        val notification = awaitSavedNotifications(1).single().notification
        val localizedContext = AppLanguageManager.wrap(context)
        assertEquals(
            localizedContext.getString(R.string.notification_resume_saved_title),
            notification.extras.getCharSequence("android.title").toString(),
        )
        assertEquals(context.packageName, notification.contentIntent.creatorPackage)
    }

    @Test
    fun repeatedSavesUpdateOneNotificationAndDisablingRemovesIt() {
        assumeTrue(manager.canPostNotifications())
        manager.enabled = true
        manager.showResumeSaved()
        manager.showResumeSaved()
        assertEquals(1, awaitSavedNotifications(1).size)

        manager.enabled = false
        assertTrue(awaitSavedNotifications(0).isEmpty())
    }

    private fun awaitSavedNotifications(expectedCount: Int): List<StatusBarNotification> {
        val deadline = SystemClock.uptimeMillis() + 3_000
        var savedNotifications = systemNotifications.activeNotifications.filter { it.id == 1001 }
        while (savedNotifications.size != expectedCount && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(100)
            savedNotifications = systemNotifications.activeNotifications.filter { it.id == 1001 }
        }
        assertEquals(expectedCount, savedNotifications.size)
        return savedNotifications
    }
}

package com.devpaul.pagocontrol

import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.domain.ProfilePreferences
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.features.home.domain.entity.Movement
import com.devpaul.pagocontrol.notifications.LocalMovementNotifier
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalMovementNotificationTest {
    @Test
    fun incomeAndExpenseAlertsSurviveSameAccountProfileChanges() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(NotificationManager::class.java)
        assumeTrue(manager.areNotificationsEnabled())
        val preferences = MutableStateFlow(ProfilePreferences(notificationsEnabled = true))
        val session = MutableStateFlow<UserSession?>(
            UserSession(
                id = "notification-test",
                name = "Test",
                email = "test@example.com"
            )
        )
        val auth = object : AuthRepository {
            override val session = session
            override suspend fun signIn(email: String, password: String) = SignInResult.Success
            override suspend fun signOut() {
                session.value = null
            }
        }
        val profile = object : ProfileRepository {
            override fun observe(userId: String) = preferences
            override suspend fun saveIdentity(userId: String, name: String, email: String) {}
            override suspend fun setNotifications(userId: String, enabled: Boolean) {
                preferences.value = preferences.value.copy(notificationsEnabled = enabled)
            }

            override suspend fun saveRating(userId: String, stars: Int, comment: String) {}
        }
        val notifier = LocalMovementNotifier(context = context, auth = auth, profiles = profile)
        notifier.start()
        delay(300)
        try {
            notifier.notify(
                Movement(
                    id = "income",
                    title = "Yape",
                    detail = "",
                    source = "Yape",
                    amountCents = 1000
                )
            )
            notifier.notify(
                Movement(
                    id = "expense",
                    title = "Bank",
                    detail = "",
                    source = "Gmail",
                    amountCents = -500
                )
            )
            delay(300)
            session.value = session.value?.copy(name = "Updated name")
            delay(300)
            val tags = manager.activeNotifications.map { it.tag }
            assertTrue(tags.contains("movement:notification-test:income"))
            assertTrue(tags.contains("movement:notification-test:expense"))
            preferences.value = preferences.value.copy(notificationsEnabled = false)
            delay(300)
            assertTrue(manager.activeNotifications.none { it.tag?.startsWith("movement:notification-test:") == true })
        } finally {
            auth.signOut()
        }
    }
}

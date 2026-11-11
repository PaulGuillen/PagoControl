package com.devpaul.pagocontrol.features.profile.ui.profile

import com.devpaul.pagocontrol.domain.ProfilePreferences
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun validatesAndSavesIdentityForCurrentUser() = runTest {
        val repository = MemoryProfile()
        val viewModel = ProfileViewModel(repository, DemoAuth())
        advanceUntilIdle()
        viewModel.onIntent(ProfileUiIntent.Open(ProfileDialog.Edit))
        viewModel.onIntent(ProfileUiIntent.Name("  Paul  "))
        viewModel.onIntent(ProfileUiIntent.Email("invalid"))
        viewModel.onIntent(ProfileUiIntent.SaveIdentity)
        advanceUntilIdle()
        assertNull(repository.preferences.value.name)
        viewModel.onIntent(ProfileUiIntent.Email("paul@example.com"))
        viewModel.onIntent(ProfileUiIntent.SaveIdentity)
        advanceUntilIdle()
        assertEquals("Paul", viewModel.state.value.name)
        assertEquals("paul@example.com", viewModel.state.value.email)
        assertEquals("demo", repository.lastUserId)
        assertNull(viewModel.state.value.dialog)
    }

    @Test fun failedNotificationWriteKeepsPreviousValueAndAllowsRetry() = runTest {
        val repository = MemoryProfile()
        val viewModel = ProfileViewModel(repository, DemoAuth())
        advanceUntilIdle()
        repository.failWrites = true
        viewModel.onIntent(ProfileUiIntent.Notifications(false))
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.notificationsEnabled)
        assertEquals(ProfileNotice.SaveError, viewModel.state.value.notice)
        assertFalse(viewModel.state.value.saving)
        repository.failWrites = false
        viewModel.onIntent(ProfileUiIntent.Dismiss)
        viewModel.onIntent(ProfileUiIntent.Notifications(false))
        advanceUntilIdle()
        val recreated = ProfileViewModel(repository, DemoAuth())
        advanceUntilIdle()
        assertFalse(recreated.state.value.notificationsEnabled)
    }

    @Test fun ratingRequiresStarsAndCancelDiscardsDraft() = runTest {
        val repository = MemoryProfile()
        val viewModel = ProfileViewModel(repository, DemoAuth())
        advanceUntilIdle()
        viewModel.onIntent(ProfileUiIntent.Open(ProfileDialog.Rating))
        viewModel.onIntent(ProfileUiIntent.SaveRating)
        advanceUntilIdle()
        assertEquals(0, repository.preferences.value.rating)
        viewModel.onIntent(ProfileUiIntent.Stars(4))
        viewModel.onIntent(ProfileUiIntent.Comment("a".repeat(600)))
        assertEquals(500, viewModel.state.value.comment.length)
        viewModel.onIntent(ProfileUiIntent.Dismiss)
        viewModel.onIntent(ProfileUiIntent.Open(ProfileDialog.Rating))
        assertEquals(0, viewModel.state.value.stars)
        assertEquals("", viewModel.state.value.comment)
        viewModel.onIntent(ProfileUiIntent.Stars(5))
        viewModel.onIntent(ProfileUiIntent.Comment("Muy útil"))
        viewModel.onIntent(ProfileUiIntent.SaveRating)
        advanceUntilIdle()
        assertEquals(5, repository.preferences.value.rating)
        assertEquals("Muy útil", repository.preferences.value.ratingComment)
        assertEquals(ProfileNotice.SavedRating, viewModel.state.value.notice)
    }

    @Test fun signOutRequiresConfirmationAndPreventsDuplicateRequests() = runTest {
        val auth = DemoAuth()
        val viewModel = ProfileViewModel(MemoryProfile(), auth)
        advanceUntilIdle()
        viewModel.onIntent(ProfileUiIntent.SignOut)
        advanceUntilIdle()
        assertEquals(0, auth.signOutCount)
        viewModel.onIntent(ProfileUiIntent.Open(ProfileDialog.SignOut))
        viewModel.onIntent(ProfileUiIntent.SignOut)
        viewModel.onIntent(ProfileUiIntent.SignOut)
        advanceUntilIdle()
        assertEquals(1, auth.signOutCount)
        assertNull(auth.session.value)
    }

    private class MemoryProfile : ProfileRepository {
        val preferences = MutableStateFlow(ProfilePreferences())
        var failWrites = false
        var lastUserId: String? = null
        override fun observe(userId: String) = preferences
        override suspend fun saveIdentity(userId: String, name: String, email: String) {
            if (failWrites) error("Unavailable")
            lastUserId = userId
            preferences.value = preferences.value.copy(name = name, email = email)
        }
        override suspend fun setNotifications(userId: String, enabled: Boolean) {
            if (failWrites) error("Unavailable")
            preferences.value = preferences.value.copy(notificationsEnabled = enabled)
        }
        override suspend fun saveRating(userId: String, stars: Int, comment: String) {
            if (failWrites) error("Unavailable")
            preferences.value = preferences.value.copy(rating = stars, ratingComment = comment)
        }
    }

    private class DemoAuth : AuthRepository {
        override val session = MutableStateFlow<UserSession?>(UserSession("demo", "Carlos", ""))
        var signOutCount = 0
        override suspend fun signIn(email: String, password: String) = SignInResult.Success
        override suspend fun signOut() { signOutCount++; session.value = null }
    }
}

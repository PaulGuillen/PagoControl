package com.example.androidbase.data

import com.example.androidbase.data.local.DemoSessionRepository
import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.example.androidbase.session.SessionState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSessionRepositoryTest {
    @Test
    fun demoSessionIsExplicitAndCanBeCleared() = runTest {
        val repository = DemoSessionRepository()
        assertEquals(SessionState.SignedOut, repository.state.value)
        repository.signIn("", "")
        val signedIn = repository.state.value as SessionState.SignedIn
        assertTrue(signedIn.user.isDemo)
        repository.signOut()
        assertEquals(SessionState.SignedOut, repository.state.value)
        assertEquals(SessionState.SignedOut, DemoSessionRepository().state.value)
    }

    @Test
    fun demoDoesNotPretendToCreateAccountsOrSendEmails() = runTest {
        val repository = DemoSessionRepository()
        assertEquals(AppResult.Failure(AppFailure.UNAVAILABLE), repository.register("test@example.com", "secret"))
        assertEquals(AppResult.Failure(AppFailure.UNAVAILABLE), repository.resetPassword("test@example.com"))
        assertEquals(SessionState.SignedOut, repository.state.value)
    }
}

package com.devpaul.pagocontrol.data

import com.devpaul.pagocontrol.data.local.DemoAuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSessionTest {
    @Test
    fun localAccessDoesNotRequireCredentials() = runTest {
        val auth = DemoAuthRepository()
        assertTrue(auth.isDemo)
        assertEquals(SignInResult.Success, auth.signIn("", ""))
        assertEquals("demo", auth.session.value?.id)
        assertEquals("", auth.session.value?.email)
    }

    @Test
    fun logoutClearsSessionAndNewInstancesStartSignedOut() = runTest {
        val auth = DemoAuthRepository()
        assertEquals(SignInResult.Success, auth.signIn("", ""))
        auth.signOut()
        assertNull(auth.session.value)
        assertNull(DemoAuthRepository().session.value)
    }
}

package com.devpaul.pagocontrol.features.home.data.repository

import com.devpaul.pagocontrol.features.home.domain.entity.DashboardResult
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoHomeRepositoryTest {
    @Test
    fun dashboardRequiresSessionAndLogoutRevokesIt() = runTest {
        val auth = object : AuthRepository {
            override val session = MutableStateFlow<UserSession?>(null)
            override suspend fun signIn(email: String, password: String): SignInResult {
                session.value = UserSession(id = "demo", name = "Test", email = "")
                return SignInResult.Success
            }

            override suspend fun signOut() {
                session.value = null
            }
        }
        val home = DemoHomeRepository(auth)
        assertEquals(DashboardResult.SessionRequired, home.loadDashboard())
        auth.signIn(email = "", password = "")
        assertTrue(home.loadDashboard() is DashboardResult.Success)
        auth.signOut()
        assertEquals(DashboardResult.SessionRequired, home.loadDashboard())
    }
}

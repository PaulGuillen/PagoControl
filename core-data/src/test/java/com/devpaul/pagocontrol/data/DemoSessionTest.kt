package com.devpaul.pagocontrol.data

import com.devpaul.pagocontrol.data.local.DemoAuthRepository
import com.devpaul.pagocontrol.data.local.DemoHomeRepository

import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.session.SignInResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSessionTest {
    @Test fun invalidCredentialsNeverCreateSession() = runTest {
        val auth = DemoAuthRepository()
        assertEquals(SignInResult.InvalidCredentials, auth.signIn("demo@pagocontrol.pe", "incorrecta"))
        assertNull(auth.session.value)
    }

    @Test fun dashboardRequiresSessionAndLogoutRevokesIt() = runTest {
        val auth = DemoAuthRepository()
        val home = DemoHomeRepository(auth)
        assertEquals(DashboardResult.SessionRequired, home.loadDashboard())
        assertEquals(SignInResult.Success, auth.signIn(" DEMO@pagocontrol.pe ", "PagoControl123"))
        assertTrue(home.loadDashboard() is DashboardResult.Success)
        auth.signOut()
        assertNull(auth.session.value)
        assertEquals(DashboardResult.SessionRequired, home.loadDashboard())
        assertNull(DemoAuthRepository().session.value)
    }
}

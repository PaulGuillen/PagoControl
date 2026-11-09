package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.domain.HomeRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun retriesFailureAndDisplaysEmptyDashboard() = runTest {
        val repository = ControlledHome()
        val viewModel = HomeViewModel(repository, DemoSession())
        advanceUntilIdle()
        assertEquals(HomeContent.Error, viewModel.state.value.content)
        repository.result = DashboardResult.Success(Dashboard(balanceCents = 0, incomeCents = 0, expenseCents = 0, incomeCount = 0, expenseCount = 0, weeklyFlow = emptyList(), monthlyFlow = emptyList(), movements = emptyList()))
        viewModel.onIntent(HomeUiIntent.Retry)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.content is HomeContent.Ready)
        viewModel.onIntent(HomeUiIntent.SelectPeriod(true))
        viewModel.onIntent(HomeUiIntent.SelectBar(6))
        assertEquals(0, viewModel.state.value.selectedBar)
    }

    @Test fun logoutIsNotDuplicated() = runTest {
        val auth = DemoSession()
        val viewModel = HomeViewModel(ControlledHome(), auth)
        viewModel.onIntent(HomeUiIntent.SignOut)
        viewModel.onIntent(HomeUiIntent.SignOut)
        advanceUntilIdle()
        assertEquals(1, auth.signOutCount)
        assertEquals(null, auth.session.value)
    }

    private class ControlledHome : HomeRepository {
        var result: DashboardResult = DashboardResult.Unavailable
        override suspend fun loadDashboard() = result
    }

    private class DemoSession : AuthRepository {
        override val session = MutableStateFlow<UserSession?>(UserSession(id = "demo", name = "Carlos", email = "demo@pagocontrol.pe"))
        var signOutCount = 0
        override suspend fun signIn(email: String, password: String) = SignInResult.Success
        override suspend fun signOut() { signOutCount++; session.value = null }
    }
}

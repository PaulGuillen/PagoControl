package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.domain.HomeRepository
import com.devpaul.pagocontrol.domain.HomeGuidePreferences
import com.devpaul.pagocontrol.domain.GmailMonitoringRepository
import com.devpaul.pagocontrol.domain.PaymentNotification
import com.devpaul.pagocontrol.domain.PaymentNotificationRepository
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
        val viewModel = HomeViewModel(home = repository, auth = DemoSession(), notifications = ControlledNotifications(), gmail = ControlledGmail(), guides = ControlledGuides())
        advanceUntilIdle()
        assertEquals(HomeContent.Error, viewModel.state.value.content)
        repository.result = DashboardResult.Success(Dashboard(balanceCents = 0, incomeCents = 0, expenseCents = 0, incomeCount = 0, expenseCount = 0, weeklyFlow = emptyList(), monthlyFlow = emptyList(), movements = emptyList()))
        viewModel.onIntent(HomeUiIntent.Retry)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.content is HomeContent.Ready)
        viewModel.onIntent(HomeUiIntent.SelectPeriod(true))
        viewModel.onIntent(HomeUiIntent.SelectBar(11))
        assertEquals(11, viewModel.state.value.selectedBar)
        viewModel.onIntent(HomeUiIntent.SelectBar(12))
        assertEquals(11, viewModel.state.value.selectedBar)
        viewModel.onIntent(HomeUiIntent.SelectPeriod(false))
        assertEquals(currentChartBar(monthly = false), viewModel.state.value.selectedBar)
    }

    @Test fun logoutIsNotDuplicated() = runTest {
        val auth = DemoSession()
        val viewModel = HomeViewModel(home = ControlledHome(), auth = auth, notifications = ControlledNotifications(), gmail = ControlledGmail(), guides = ControlledGuides())
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

    @Test fun observesNotificationsAndPersistsReviewActions() = runTest {
        val notifications = ControlledNotifications()
        val viewModel = HomeViewModel(home = ControlledHome(), auth = DemoSession(), notifications = notifications, gmail = ControlledGmail(), guides = ControlledGuides())
        advanceUntilIdle()
        notifications.pending.value = listOf(PaymentNotification(
            id = "payment", packageName = "test.bank", source = "Banco", title = "Pago",
            text = "Recibiste S/ 25.00", postedAt = 1L, suggestedAmountCents = 2500L,
        ))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.pendingNotifications.size)
        viewModel.onIntent(HomeUiIntent.ConfirmPayment(id = "payment", amountCents = 2500L))
        advanceUntilIdle()
        assertEquals("payment" to 2500L, notifications.confirmed)
        assertTrue(viewModel.state.value.pendingNotifications.isEmpty())
    }

    @Test fun keepsGmailMonitoringAndErrorsIndependentFromWallets() = runTest {
        val gmail = ControlledGmail()
        val viewModel = HomeViewModel(home = ControlledHome(), auth = DemoSession(), notifications = ControlledNotifications(), gmail = gmail, guides = ControlledGuides())
        advanceUntilIdle()
        viewModel.onIntent(HomeUiIntent.SetGmailMonitoring(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.gmailMonitoring)
        assertTrue(viewModel.state.value.notificationSources.isEmpty())
        gmail.fail = true
        viewModel.onIntent(HomeUiIntent.SetGmailMonitoring(false))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.gmailError)
        assertEquals(false, viewModel.state.value.notificationError)
        assertEquals(false, viewModel.state.value.savingNotification)
    }

    @Test fun guidesAreIndependentPersistedAndReplayable() = runTest {
        val guides = ControlledGuides()
        val viewModel = HomeViewModel(home = ControlledHome(), auth = DemoSession(), notifications = ControlledNotifications(), gmail = ControlledGmail(), guides = guides)
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.guideStep)
        viewModel.onIntent(HomeUiIntent.NextGuideStep)
        assertEquals(1, viewModel.state.value.guideStep)
        viewModel.onIntent(HomeUiIntent.PreviousGuideStep)
        assertEquals(0, viewModel.state.value.guideStep)
        repeat(4) { viewModel.onIntent(HomeUiIntent.NextGuideStep) }
        advanceUntilIdle()
        assertEquals(null, viewModel.state.value.guideStep)
        assertEquals(setOf("wallets"), guides.completedGuides.value)
        viewModel.onIntent(HomeUiIntent.SelectSection(HomeSection.Mail))
        assertEquals(0, viewModel.state.value.guideStep)
        viewModel.onIntent(HomeUiIntent.DismissGuide)
        advanceUntilIdle()
        assertEquals(setOf("wallets", "mail"), guides.completedGuides.value)
        viewModel.onIntent(HomeUiIntent.SelectSection(HomeSection.Wallets))
        assertEquals(null, viewModel.state.value.guideStep)
        viewModel.onIntent(HomeUiIntent.ShowGuide)
        assertEquals(0, viewModel.state.value.guideStep)
        val reopened = HomeViewModel(home = ControlledHome(), auth = DemoSession(), notifications = ControlledNotifications(), gmail = ControlledGmail(), guides = guides)
        advanceUntilIdle()
        assertEquals(null, reopened.state.value.guideStep)
    }

    @Test fun refreshingSameDatePreservesManualBarSelection() = runTest {
        val viewModel = HomeViewModel(home = ControlledHome(), auth = DemoSession(), notifications = ControlledNotifications(), gmail = ControlledGmail(), guides = ControlledGuides())
        viewModel.onIntent(HomeUiIntent.SelectBar(0))
        viewModel.onIntent(HomeUiIntent.RefreshDate)
        assertEquals(0, viewModel.state.value.selectedBar)
    }

    private class ControlledGuides : HomeGuidePreferences {
        override val completedGuides = MutableStateFlow<Set<String>>(emptySet())

        override suspend fun completeGuide(section: String) {
            completedGuides.value += section
        }
    }

    private class ControlledGmail : GmailMonitoringRepository {
        val enabled = MutableStateFlow(false)
        var fail = false

        override fun observeGmailDashboard() = MutableStateFlow<DashboardResult>(DashboardResult.Unavailable)

        override fun observeGmailPending() = MutableStateFlow<List<PaymentNotification>>(emptyList())

        override fun observeGmailEnabled() = enabled

        override suspend fun setGmailEnabled(enabled: Boolean) {
            if (fail) error("Gmail storage unavailable")
            this.enabled.value = enabled
        }
    }

    private class ControlledNotifications : PaymentNotificationRepository {
        val pending = MutableStateFlow<List<PaymentNotification>>(emptyList())
        val sources = MutableStateFlow<Set<String>>(emptySet())
        var confirmed: Pair<String, Long>? = null

        override fun observePending() = pending

        override fun observeSources() = sources

        override suspend fun setSource(packageName: String, enabled: Boolean) {
            sources.value = if (enabled) sources.value + packageName else sources.value - packageName
        }

        override suspend fun capture(notification: PaymentNotification) { pending.value += notification }

        override suspend fun confirm(id: String, amountCents: Long) {
            confirmed = id to amountCents
            pending.value = pending.value.filterNot { it.id == id }
        }

        override suspend fun discard(id: String) { pending.value = pending.value.filterNot { it.id == id } }
    }

    private class DemoSession : AuthRepository {
        override val session = MutableStateFlow<UserSession?>(UserSession(id = "demo", name = "Carlos", email = "demo@pagocontrol.pe"))
        var signOutCount = 0

        override suspend fun signIn(email: String, password: String) = SignInResult.Success

        override suspend fun signOut() { signOutCount++; session.value = null }
    }
}

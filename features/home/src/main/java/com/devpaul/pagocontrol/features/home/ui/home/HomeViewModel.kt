package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.platform.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.domain.HomeRepository
import com.devpaul.pagocontrol.domain.GmailMonitoringRepository
import com.devpaul.pagocontrol.domain.PaymentNotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val home: HomeRepository,
    private val auth: AuthRepository,
    private val notifications: PaymentNotificationRepository,
    private val gmail: GmailMonitoringRepository,
) : BaseViewModel<HomeUiState>(HomeUiState()) {
    private var chartDate = currentChartDate()
    private var dashboardJob: Job? = null
    private var pendingJob: Job? = null
    private var sourcesJob: Job? = null
    private var gmailJobs: List<Job> = emptyList()

    init { load(); observeNotifications(); observeGmail() }

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            HomeUiIntent.RefreshDate -> {
                val today = currentChartDate()
                if (today != chartDate) {
                    chartDate = today
                    updateState { it.copy(selectedBar = currentChartBar(it.monthly)) }
                }
            }
            is HomeUiIntent.SelectSection -> updateState { it.copy(section = intent.section) }
            HomeUiIntent.Retry -> { load(); observeNotifications(); observeGmail() }
            is HomeUiIntent.SelectPeriod -> updateState {
                it.copy(monthly = intent.monthly, selectedBar = currentChartBar(intent.monthly))
            }
            is HomeUiIntent.SelectBar -> {
                val count = if (state.value.monthly) 5 else 7
                if (intent.index in 0 until count) updateState { it.copy(selectedBar = intent.index) }
            }
            HomeUiIntent.Upcoming -> updateState { it.copy(showUpcoming = true) }
            HomeUiIntent.DismissUpcoming -> updateState { it.copy(showUpcoming = false) }
            HomeUiIntent.SignOut -> signOut()
            is HomeUiIntent.SetNotificationSource -> saveNotification {
                notifications.setSource(intent.packageName, intent.enabled)
            }
            is HomeUiIntent.ConfirmPayment -> saveNotification(mail = intent.mail) {
                notifications.confirm(intent.id, intent.amountCents)
            }
            is HomeUiIntent.DiscardNotification -> saveNotification(mail = intent.mail) { notifications.discard(intent.id) }
            is HomeUiIntent.SetGmailMonitoring -> saveNotification(mail = true) { gmail.setGmailEnabled(intent.enabled) }
        }
    }

    private fun load() {
        dashboardJob?.cancel()
        updateState { it.copy(content = HomeContent.Loading) }
        dashboardJob = viewModelScope.launch {
            try {
                home.observeDashboard().collect { result ->
                    updateState {
                        it.copy(content = when (result) {
                            is DashboardResult.Success -> HomeContent.Ready(result.dashboard)
                            DashboardResult.SessionRequired, DashboardResult.Unavailable -> HomeContent.Error
                        })
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(content = HomeContent.Error) }
            }
        }
    }

    private fun observeNotifications() {
        updateState { it.copy(notificationError = false) }
        pendingJob?.cancel()
        sourcesJob?.cancel()
        pendingJob = viewModelScope.launch {
            try {
                notifications.observePending().collect { pending -> updateState { it.copy(pendingNotifications = pending) } }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(notificationError = true) }
            }
        }
        sourcesJob = viewModelScope.launch {
            try {
                notifications.observeSources().collect { sources -> updateState { it.copy(notificationSources = sources) } }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(notificationError = true) }
            }
        }
    }

    private fun observeGmail() {
        gmailJobs.forEach { it.cancel() }
        updateState { it.copy(gmailError = false) }
        gmailJobs = listOf(
            observeGmailFlow {
                gmail.observeGmailDashboard().collect { result ->
                    updateState { it.copy(gmailContent = when (result) {
                        is DashboardResult.Success -> HomeContent.Ready(result.dashboard)
                        else -> HomeContent.Error
                    }) }
                }
            },
            observeGmailFlow {
                gmail.observeGmailPending().collect { pending -> updateState { it.copy(gmailPendingNotifications = pending) } }
            },
            observeGmailFlow {
                gmail.observeGmailEnabled().collect { enabled -> updateState { it.copy(gmailMonitoring = enabled) } }
            },
        )
    }

    private fun observeGmailFlow(operation: suspend () -> Unit): Job = viewModelScope.launch {
        try {
            operation()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            updateState { it.copy(gmailError = true, gmailContent = HomeContent.Error) }
        }
    }

    private fun saveNotification(mail: Boolean = false, operation: suspend () -> Unit) {
        if (state.value.savingNotification) return
        updateState { it.copy(savingNotification = true, notificationError = if (mail) it.notificationError else false, gmailError = if (mail) false else it.gmailError) }
        viewModelScope.launch {
            try {
                operation()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { if (mail) it.copy(gmailError = true) else it.copy(notificationError = true) }
            } finally {
                updateState { it.copy(savingNotification = false) }
            }
        }
    }

    private fun signOut() {
        if (state.value.signingOut) return
        updateState { it.copy(signingOut = true) }
        viewModelScope.launch {
            try {
                auth.signOut()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(content = HomeContent.Error) }
            } finally {
                updateState { it.copy(signingOut = false) }
            }
        }
    }
}

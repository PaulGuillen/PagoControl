package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.platform.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.domain.HomeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(private val home: HomeRepository, private val auth: AuthRepository) : BaseViewModel<HomeUiState>(HomeUiState()) {
    private var loading = false

    init { load() }

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            HomeUiIntent.Retry -> load()
            is HomeUiIntent.SelectPeriod -> updateState {
                it.copy(monthly = intent.monthly, selectedBar = if (intent.monthly) 0 else 5)
            }
            is HomeUiIntent.SelectBar -> {
                val count = if (state.value.monthly) 4 else 7
                if (intent.index in 0 until count) updateState { it.copy(selectedBar = intent.index) }
            }
            HomeUiIntent.ConfirmNotification -> updateState { it.copy(confirmed = true) }
            HomeUiIntent.Upcoming -> updateState { it.copy(showUpcoming = true) }
            HomeUiIntent.DismissUpcoming -> updateState { it.copy(showUpcoming = false) }
            HomeUiIntent.SignOut -> signOut()
        }
    }

    private fun load() {
        if (loading) return
        loading = true
        updateState { it.copy(content = HomeContent.Loading) }
        viewModelScope.launch {
            try {
                val result = home.loadDashboard()
                updateState {
                    it.copy(content = when (result) {
                        is DashboardResult.Success -> HomeContent.Ready(result.dashboard)
                        DashboardResult.SessionRequired, DashboardResult.Unavailable -> HomeContent.Error
                    })
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(content = HomeContent.Error) }
            } finally {
                loading = false
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

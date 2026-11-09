package com.example.androidbase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbase.domain.AppPreferences
import com.example.androidbase.platform.EventReporter
import com.example.androidbase.session.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val session: SessionRepository,
    preferences: AppPreferences,
    reporter: EventReporter,
) : ViewModel() {
    val sessionState = session.state
    val darkTheme = preferences.darkTheme.catch {
        reporter.error("preferences_read")
        emit(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun signOut() = session.signOut()
}

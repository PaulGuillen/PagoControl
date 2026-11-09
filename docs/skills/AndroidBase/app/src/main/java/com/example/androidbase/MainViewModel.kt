package com.example.androidbase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbase.domain.AppPreferences
import com.example.androidbase.platform.EventReporter
import com.example.androidbase.session.SessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val session: SessionRepository,
    private val preferences: AppPreferences,
    private val reporter: EventReporter,
) : ViewModel() {
    val sessionState = session.state
    val darkTheme = preferences.darkTheme.catch {
        reporter.error("preferences_read")
        emit(false)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = false)

    fun selectTheme(index: Int) {
        viewModelScope.launch {
            try { preferences.setDarkTheme(index == 2) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { reporter.error("theme_write") }
        }
    }

    fun signOut() = session.signOut()
}

package com.example.androidbase.features.home.ui.home

import androidx.lifecycle.viewModelScope
import com.example.androidbase.domain.AddExampleUseCase
import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppPreferences
import com.example.androidbase.domain.AppResult
import com.example.androidbase.domain.ExampleRepository
import com.example.androidbase.platform.BaseViewModel
import com.example.androidbase.platform.EventReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class HomeViewModel(
    @InjectedParam private val ownerId: String,
    private val repository: ExampleRepository,
    private val addExample: AddExampleUseCase,
    private val preferences: AppPreferences,
    private val reporter: EventReporter,
) : BaseViewModel<HomeUiState>(HomeUiState()) {
    private var observation: Job? = null

    init { observe() }

    fun accept(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.Add -> add(intent.title)
            is HomeUiIntent.SetDarkTheme -> viewModelScope.launch {
                try {
                    preferences.setDarkTheme(intent.enabled)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    reporter.error("theme_write")
                    updateState { it.copy(failure = AppFailure.UNKNOWN) }
                }
            }
            HomeUiIntent.Retry -> observe()
        }
    }

    private fun observe() {
        observation?.cancel()
        updateState { it.copy(isLoading = true, failure = null) }
        observation = viewModelScope.launch {
            repository.observe(ownerId).catch {
                reporter.error("items_observe")
                updateState { it.copy(isLoading = false, failure = AppFailure.UNKNOWN) }
            }.collect { items ->
                updateState { it.copy(items = items, isLoading = false) }
            }
        }
    }

    private fun add(title: String) {
        if (state.value.isSaving) return
        updateState { it.copy(isSaving = true, failure = null) }
        viewModelScope.launch {
            when (val result = addExample(ownerId, title)) {
                is AppResult.Success -> {
                    reporter.action("item_added")
                    updateState { it.copy(isSaving = false, savedCount = it.savedCount + 1) }
                }
                is AppResult.Failure -> {
                    reporter.error("item_add")
                    updateState { it.copy(isSaving = false, failure = result.reason) }
                }
            }
        }
    }
}

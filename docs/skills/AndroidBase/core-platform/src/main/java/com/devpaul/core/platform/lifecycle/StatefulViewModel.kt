package com.devpaul.core.platform.lifecycle

import com.devpaul.core.platform.lifecycle.base.UiStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class StatefulViewModel<UiState, UiIntent, UiEvent>(
    initialState: UiState,
) : StatelessViewModel<UiIntent, UiEvent>(), UiStateHolder<UiState> {

    private val mutableUiState = MutableStateFlow(initialState)
    final override val uiState: StateFlow<UiState> = mutableUiState.asStateFlow()

    protected fun updateUiState(reducer: (UiState) -> UiState) {
        mutableUiState.value = reducer(mutableUiState.value)
    }
}

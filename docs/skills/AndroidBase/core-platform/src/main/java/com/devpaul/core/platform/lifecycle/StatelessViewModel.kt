package com.devpaul.core.platform.lifecycle

import androidx.lifecycle.ViewModel
import com.devpaul.core.platform.lifecycle.base.UiEventHolder
import com.devpaul.core.platform.lifecycle.base.UiIntentHolder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.collect

abstract class StatelessViewModel<UiIntent, UiEvent> : ViewModel(),
    UiIntentHolder<UiIntent>,
    UiEventHolder<UiEvent> {

    private val uiIntentChannel = Channel<UiIntent>(Channel.BUFFERED)
    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)

    final override val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    init {
        launch {
            uiIntentChannel.receiveAsFlow().collect(::onUiIntent)
        }
    }

    final override fun executeUiIntent(intent: UiIntent) {
        uiIntentChannel.trySend(intent)
    }

    protected fun emitUiEvent(event: UiEvent) {
        _uiEvents.tryEmit(event)
    }
}

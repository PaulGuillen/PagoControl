package com.devpaul.features.resume.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
internal fun <Draft> TrackResumeDraft(value: Draft, onDraftChange: (Draft) -> Unit) {
    val currentOnDraftChange by rememberUpdatedState(onDraftChange)
    LaunchedEffect(value) {
        currentOnDraftChange(value)
    }
}

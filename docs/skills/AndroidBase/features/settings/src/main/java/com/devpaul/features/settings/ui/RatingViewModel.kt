package com.devpaul.features.settings.ui

import androidx.lifecycle.ViewModel
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.lifecycle.viewModelScope
import com.devpaul.core.domain.rating.MAX_RATING_COMMENT_LENGTH
import com.devpaul.core.domain.rating.RatingRepository
import com.devpaul.core.domain.rating.RatingResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

enum class RatingDialogStatus {
    Closed,
    Checking,
    Editing,
    Sending,
    Submitted,
    DailyLimitReached,
}

data class RatingUiState(
    val status: RatingDialogStatus = RatingDialogStatus.Closed,
    val stars: Int = 0,
    val comment: String = "",
    val hasError: Boolean = false,
)

@KoinViewModel
open class RatingViewModel(private val repository: RatingRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(RatingUiState())
    val state = mutableState.asStateFlow()

    fun open() {
        if (mutableState.value.status != RatingDialogStatus.Closed) return
        mutableState.value = mutableState.value.copy(status = RatingDialogStatus.Checking, hasError = false)
        viewModelScope.launch {
            try {
                val status = if (repository.availability() == RatingResult.DailyLimitReached) {
                    RatingDialogStatus.DailyLimitReached
                } else {
                    RatingDialogStatus.Editing
                }
                mutableState.value = mutableState.value.copy(status = status)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppAnalytics.error("check_rating_availability", error)
                mutableState.value = mutableState.value.copy(status = RatingDialogStatus.Editing, hasError = true)
            }
        }
    }

    fun selectStars(stars: Int) {
        if (mutableState.value.status == RatingDialogStatus.Editing && stars in 1..5) {
            mutableState.value = mutableState.value.copy(stars = stars, hasError = false)
        }
    }

    fun updateComment(comment: String) {
        if (mutableState.value.status == RatingDialogStatus.Editing) {
            mutableState.value = mutableState.value.copy(comment = comment.take(MAX_RATING_COMMENT_LENGTH), hasError = false)
        }
    }

    fun dismiss() {
        if (mutableState.value.status in listOf(RatingDialogStatus.Checking, RatingDialogStatus.Sending)) return
        mutableState.value = mutableState.value.copy(status = RatingDialogStatus.Closed)
    }

    fun submit() {
        val current = mutableState.value
        if (current.status != RatingDialogStatus.Editing || current.stars !in 1..5) return
        mutableState.value = current.copy(status = RatingDialogStatus.Sending, hasError = false)
        viewModelScope.launch {
            try {
                val result = repository.submit(current.stars, current.comment)
                AppAnalytics.outcome("submit_rating", if (result == RatingResult.Submitted) "success" else "daily_limit")
                mutableState.value = if (result == RatingResult.Submitted) {
                    RatingUiState(status = RatingDialogStatus.Submitted)
                } else {
                    current.copy(status = RatingDialogStatus.DailyLimitReached)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppAnalytics.error("submit_rating", error)
                mutableState.value = current.copy(hasError = true)
            }
        }
    }
}

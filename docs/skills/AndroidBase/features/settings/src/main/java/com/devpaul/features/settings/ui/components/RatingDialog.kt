package com.devpaul.features.settings.ui.components

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.analytics.TrackAnalyticsView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.rating.MAX_RATING_COMMENT_LENGTH
import com.devpaul.features.settings.R
import com.devpaul.features.settings.ui.RatingDialogStatus
import com.devpaul.features.settings.ui.RatingUiState

@Composable
internal fun RatingDialog(
    state: RatingUiState,
    onStarsSelected: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (state.status == RatingDialogStatus.Closed) return
    TrackAnalyticsView("rating_dialog")
    val isChecking = state.status == RatingDialogStatus.Checking
    val isSending = state.status == RatingDialogStatus.Sending
    val isBusy = isChecking || isSending
    val isNotice = state.status in listOf(RatingDialogStatus.Submitted, RatingDialogStatus.DailyLimitReached)

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text(stringResource(R.string.settings_rate)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when {
                    isChecking -> {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Text(stringResource(R.string.rating_checking))
                    }
                    isNotice -> Text(
                        stringResource(
                            if (state.status == RatingDialogStatus.Submitted) R.string.rating_success
                            else R.string.rating_daily_limit,
                        ),
                    )
                    else -> {
                        Text(stringResource(R.string.rating_intro))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(stringResource(R.string.rating_not_recommended), style = MaterialTheme.typography.labelMedium)
                            Text(stringResource(R.string.rating_recommended), style = MaterialTheme.typography.labelMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth().selectableGroup()) {
                            (1..5).forEach { stars ->
                                val description = stringResource(R.string.rating_stars_description, stars)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { contentDescription = description }
                                        .selectable(
                                            selected = state.stars == stars,
                                            enabled = !isSending,
                                            role = Role.RadioButton,
                                            onClick = trackedClick("select_rating") {
                                                AppAnalytics.selection("rating_stars", stars.toString())
                                                onStarsSelected(stars)
                                            },
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = if (stars <= state.stars) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Text(stars.toString(), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = state.comment,
                            onValueChange = onCommentChange,
                            enabled = !isSending,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.rating_comment)) },
                            placeholder = { Text(stringResource(R.string.rating_comment_hint)) },
                            minLines = 3,
                            maxLines = 4,
                            supportingText = { Text("${state.comment.length}/$MAX_RATING_COMMENT_LENGTH") },
                        )
                        Text(
                            text = stringResource(R.string.rating_frequency),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (state.hasError) {
                            Text(
                                text = stringResource(R.string.rating_error),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isNotice) {
                TextButton(onClick = trackedClick("close_rating", onDismiss)) { Text(stringResource(R.string.rating_ok)) }
            } else if (!isChecking) {
                Button(onClick = trackedClick("submit_rating", onSubmit), enabled = !isSending && state.stars in 1..5) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(if (isSending) R.string.rating_sending else R.string.rating_send))
                }
            }
        },
        dismissButton = {
            if (!isNotice && !isChecking) {
                TextButton(onClick = trackedClick("cancel_rating", onDismiss), enabled = !isSending) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        },
    )
}

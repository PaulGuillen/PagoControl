package com.devpaul.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.rating.MAX_RATING_COMMENT_LENGTH
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.features.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlanScreen(
    feedbackState: RatingUiState,
    onBack: () -> Unit,
    onStarsSelected: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
) {
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)
    Scaffold(
        modifier = modifier,
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_my_plan)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.plan_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                PlanCard(highlighted = true) {
                    Text(stringResource(R.string.plan_current), style = MaterialTheme.typography.labelLarge)
                    Text(
                        stringResource(if (isPremium) R.string.settings_plan_premium else R.string.settings_plan_free),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(stringResource(if (isPremium) R.string.plan_debug_override else R.string.plan_all_free))
                }
            }
            item {
                PlanCard {
                    Icon(Icons.Outlined.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.plan_under_construction),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.plan_future_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(R.string.plan_future_intro))
                    Text(stringResource(R.string.plan_future_writing), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.plan_future_writing_body))
                    Text(stringResource(R.string.plan_future_review), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.plan_future_review_body))
                    Text(stringResource(R.string.plan_future_design), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.plan_future_design_body))
                    Text(stringResource(R.string.plan_future_note), style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                PlanCard {
                    Text(
                        stringResource(R.string.plan_feedback_question),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(R.string.plan_feedback_intro))
                    PremiumFeedbackForm(feedbackState, onStarsSelected, onCommentChange, onSubmit)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(highlighted: Boolean = false, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = if (highlighted) 0.dp else 2.dp,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun PremiumFeedbackForm(
    state: RatingUiState,
    onStarsSelected: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    when (state.status) {
        RatingDialogStatus.Closed, RatingDialogStatus.Checking -> {
            CircularProgressIndicator(Modifier.size(24.dp))
            Text(stringResource(R.string.plan_feedback_checking))
        }
        RatingDialogStatus.Submitted -> Text(stringResource(R.string.plan_feedback_success))
        RatingDialogStatus.DailyLimitReached -> Text(stringResource(R.string.plan_feedback_daily_limit))
        RatingDialogStatus.Editing, RatingDialogStatus.Sending -> {
            val isSending = state.status == RatingDialogStatus.Sending
            Row(Modifier.fillMaxWidth().selectableGroup()) {
                for (stars in 1..5) {
                    val description = stringResource(R.string.plan_feedback_stars, stars)
                    Column(
                        modifier = Modifier.weight(1f)
                            .semantics { contentDescription = description }
                            .selectable(
                                selected = state.stars == stars,
                                enabled = !isSending,
                                role = Role.RadioButton,
                                onClick = { onStarsSelected(stars) },
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            if (stars <= state.stars) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(stars.toString(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Text(stringResource(R.string.plan_feedback_scale), style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = state.comment,
                onValueChange = onCommentChange,
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.rating_comment)) },
                placeholder = { Text(stringResource(R.string.plan_feedback_hint)) },
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${state.comment.length}/$MAX_RATING_COMMENT_LENGTH") },
            )
            Text(stringResource(R.string.plan_feedback_frequency), style = MaterialTheme.typography.bodySmall)
            if (state.hasError) {
                Text(stringResource(R.string.plan_feedback_error), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onSubmit,
                enabled = !isSending && state.stars in 1..5,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSending) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(stringResource(if (isSending) R.string.rating_sending else R.string.plan_feedback_send))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPlanPreview() {
    CvFreeTheme {
        MyPlanScreen(RatingUiState(status = RatingDialogStatus.Editing, stars = 4), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPlanDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        MyPlanScreen(RatingUiState(status = RatingDialogStatus.Submitted), {}, {}, {}, {})
    }
}

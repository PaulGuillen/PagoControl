package com.devpaul.features.settings.ui

import com.devpaul.core.platform.analytics.trackedClick
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.features.settings.R

private data class HelpQuestion(
    @param:StringRes val question: Int,
    @param:StringRes val answer: Int,
)

private val helpQuestions = listOf(
    HelpQuestion(R.string.help_create_question, R.string.help_create_answer),
    HelpQuestion(R.string.help_sections_question, R.string.help_sections_answer),
    HelpQuestion(R.string.help_edit_question, R.string.help_edit_answer),
    HelpQuestion(R.string.help_templates_question, R.string.help_templates_answer),
    HelpQuestion(R.string.help_pdf_question, R.string.help_pdf_answer),
    HelpQuestion(R.string.help_import_question, R.string.help_import_answer),
    HelpQuestion(R.string.help_scan_question, R.string.help_scan_answer),
    HelpQuestion(R.string.help_autosave_question, R.string.help_autosave_answer),
    HelpQuestion(R.string.help_delete_question, R.string.help_delete_answer),
    HelpQuestion(R.string.help_language_question, R.string.help_language_answer),
    HelpQuestion(R.string.help_appearance_question, R.string.help_appearance_answer),
    HelpQuestion(R.string.help_developer_question, R.string.help_developer_answer),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedQuestion by rememberSaveable { mutableStateOf<Int?>(null) }
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Scaffold(
        modifier = modifier,
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_help),
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = trackedClick("back", onBack)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.help_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = stringResource(R.string.help_intro_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.help_intro_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.help_faq_title),
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(helpQuestions, key = { it.question }) { question ->
                HelpQuestionCard(
                    question = question,
                    expanded = expandedQuestion == question.question,
                    onClick = trackedClick("toggle_help_question") {
                        expandedQuestion = if (expandedQuestion == question.question) null else question.question
                    },
                )
            }
            item {
                Text(
                    text = stringResource(R.string.help_developer_credit),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HelpQuestionCard(
    question: HelpQuestion,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "helpArrow")
    val expansionState = stringResource(if (expanded) R.string.help_expanded else R.string.help_collapsed)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { stateDescription = expansionState }
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(question.question),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = stringResource(question.answer),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Help Center Light", showBackground = true)
@Composable
private fun HelpCenterLightPreview() {
    CvFreeTheme(darkTheme = false) {
        HelpCenterScreen(onBack = {})
    }
}

@Preview(name = "Help Center Dark", showBackground = true)
@Composable
private fun HelpCenterDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        HelpCenterScreen(onBack = {})
    }
}

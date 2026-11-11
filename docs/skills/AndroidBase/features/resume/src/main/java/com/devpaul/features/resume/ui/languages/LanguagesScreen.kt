package com.devpaul.features.resume.ui.languages

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeLanguage
import com.devpaul.core.domain.resume.newResumeEntryId
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.ui.components.BuilderAddButton
import com.devpaul.features.resume.ui.components.BuilderEditorActions
import com.devpaul.features.resume.ui.components.BuilderNewEntryCard
import com.devpaul.features.resume.ui.components.BuilderPage
import com.devpaul.features.resume.ui.components.TrackResumeDraft
import com.devpaul.features.resume.ui.components.BuilderSavedEntryCard
import com.devpaul.features.resume.ui.components.NewEntryId
import com.devpaul.features.resume.ui.components.replaceById

@Composable
fun LanguagesScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    step: Int,
    totalSteps: Int,
    actionText: String,
    modifier: Modifier = Modifier,
    selectedLanguages: List<ResumeLanguage> = emptyList(),
    onLanguagesChange: (List<ResumeLanguage>) -> Unit = {},
    onDraftChange: (ResumeLanguage?) -> Unit = {},
    onDraftFinished: () -> Unit = {},
) {
    var editingId by rememberSaveable {
        mutableStateOf(if (selectedLanguages.isEmpty()) NewEntryId else null)
    }
    var selectedLanguage by rememberSaveable { mutableStateOf(availableLanguages.first()) }
    var selectedLevel by rememberSaveable { mutableStateOf(availableLevels.last()) }
    var editorTouched by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun resetEditor() {
        selectedLanguage = availableLanguages.first()
        selectedLevel = availableLevels.last()
        editorTouched = false
        showErrors = false
    }

    fun startNew() {
        onDraftFinished()
        resetEditor()
        editingId = NewEntryId
    }

    fun startEditing(item: ResumeLanguage) {
        onDraftFinished()
        selectedLanguage = item.language
        selectedLevel = item.level
        editorTouched = true
        showErrors = false
        editingId = item.id
    }

    val draftEntryId = rememberSaveable(editingId) { newResumeEntryId() }
    if (editingId != null) {
        val pendingEntry = if (editorTouched || editingId != NewEntryId) {
            ResumeLanguage(
                language = selectedLanguage,
                level = selectedLevel,
                id = editingId?.takeUnless { it == NewEntryId } ?: draftEntryId,
            )
        } else {
            null
        }
        TrackResumeDraft(pendingEntry, onDraftChange)
    } else {
        TrackResumeDraft(Unit) { onDraftFinished() }
    }

    fun saveEditor(): Boolean {
        showErrors = true
        val duplicate = selectedLanguages.any {
            it.id != editingId && sameLanguage(it.language, selectedLanguage)
        }
        if (duplicate) {
            AppAnalytics.error("validate_language", code = "duplicate_language")
            return false
        }

        val currentId = editingId
        val value = ResumeLanguage(
            language = selectedLanguage,
            level = selectedLevel,
            id = currentId?.takeUnless { it == NewEntryId } ?: draftEntryId,
        )
        onLanguagesChange(
            if (currentId == NewEntryId) listOf(value) + selectedLanguages
            else selectedLanguages.replaceById(value.id, ResumeLanguage::id, value),
        )
        editingId = null
        resetEditor()
        return true
    }

    fun continueFlow() {
        when {
            editingId == null -> onContinue()
            editingId == NewEntryId && !editorTouched -> {
                editingId = null
                onContinue()
            }
            saveEditor() -> onContinue()
        }
    }

    BuilderPage(
        onBack = onBack,
        step = step,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_languages_553733),
        title = stringResource(R.string.ui_languages_553733),
        description = stringResource(R.string.ui_list_the_languages_you_know_and_your_proficiency_level_179a0c),
        actionText = actionText,
        onContinue = ::continueFlow,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editingId == NewEntryId) {
                BuilderNewEntryCard(
                    title = stringResource(R.string.ui_new_language_014fc6),
                    bringIntoView = selectedLanguages.isNotEmpty(),
                ) {
                    Spacer(Modifier.height(16.dp))
                    LanguageEditor(
                        selectedLanguage = selectedLanguage,
                        onLanguageChange = {
                            selectedLanguage = it
                            editorTouched = true
                        },
                        selectedLevel = selectedLevel,
                        onLevelChange = {
                            selectedLevel = it
                            editorTouched = true
                        },
                        duplicate = showErrors && selectedLanguages.any {
                            sameLanguage(it.language, selectedLanguage)
                        },
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                        onSave = ::saveEditor,
                    )
                }
            }

            selectedLanguages.forEach { item ->
                val expanded = editingId == item.id
                BuilderSavedEntryCard(
                    title = languageLabel(item.language),
                    subtitle = proficiencyLabel(item.level),
                    expanded = expanded,
                    onEdit = { if (expanded) editingId = null else startEditing(item) },
                    onDelete = {
                        onLanguagesChange(selectedLanguages.filterNot { it.id == item.id })
                        if (expanded) {
                            editingId = null
                            resetEditor()
                        }
                    },
                    editor = if (expanded) {
                        {
                            LanguageEditor(
                                selectedLanguage = selectedLanguage,
                                onLanguageChange = {
                                    selectedLanguage = it
                                    editorTouched = true
                                },
                                selectedLevel = selectedLevel,
                                onLevelChange = {
                                    selectedLevel = it
                                    editorTouched = true
                                },
                                duplicate = showErrors && selectedLanguages.any {
                                    it.id != editingId && sameLanguage(it.language, selectedLanguage)
                                },
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                                onSave = ::saveEditor,
                            )
                        }
                    } else null,
                )
            }
            if (editingId == null) {
                BuilderAddButton(text = stringResource(R.string.ui_add_language_4fe149), onClick = ::startNew)
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ) {
            Text(
                text = stringResource(R.string.ui_be_precise_about_your_level_some_companies_may_verify_it_dur_965ff4),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LanguageEditor(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    selectedLevel: String,
    onLevelChange: (String) -> Unit,
    duplicate: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    Text(stringResource(R.string.ui_language_009433), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = trackedClick("open_language_options") { menuExpanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (duplicate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(languageLabel(selectedLanguage), modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(R.string.ui_select_language_566b16))
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            availableLanguages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(languageLabel(language)) },
                    onClick = trackedClick("select_language") {
                        onLanguageChange(language)
                        menuExpanded = false
                    },
                )
            }
        }
    }
    if (duplicate) {
        Text(
            text = stringResource(R.string.ui_this_language_has_already_been_added_303f72),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.ui_level_ada9db), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        availableLevels.chunked(2).forEach { rowLevels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowLevels.forEach { level ->
                    val selected = sameProficiency(level, selectedLevel)
                    Surface(
                        onClick = trackedClick("select_language_level") { onLevelChange(level) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = proficiencyLabel(level),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    BuilderEditorActions(onCancel = onCancel, onSave = onSave)
}

@Preview(name = "Languages Light", showBackground = true, heightDp = 900)
@Composable
private fun LanguagesLightPreview() {
    CvFreeTheme(darkTheme = false) { LanguagesScreen({}, {}, 7, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

@Preview(name = "Languages Dark", showBackground = true, heightDp = 900)
@Composable
private fun LanguagesDarkPreview() {
    CvFreeTheme(darkTheme = true) { LanguagesScreen({}, {}, 7, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

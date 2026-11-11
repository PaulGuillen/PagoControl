package com.devpaul.features.resume.ui.experience

import androidx.compose.foundation.clickable
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.newResumeEntryId
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.ui.components.BuilderAddButton
import com.devpaul.features.resume.ui.components.BuilderEditorActions
import com.devpaul.features.resume.ui.components.BuilderNewEntryCard
import com.devpaul.features.resume.ui.components.BuilderPage
import com.devpaul.features.resume.ui.components.TrackResumeDraft
import com.devpaul.features.resume.ui.components.BuilderSavedEntryCard
import com.devpaul.features.resume.ui.components.BuilderTextField
import com.devpaul.features.resume.ui.components.NewEntryId
import com.devpaul.features.resume.ui.components.isValidMonthYearOrBlank
import com.devpaul.features.resume.ui.components.replaceById
import com.devpaul.features.resume.ui.summary.RichTextEditor
import com.devpaul.features.resume.ui.summary.RichTextEditorState
import com.devpaul.features.templates.ui.localizedEndYear

@Composable
fun ExperienceScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 5,
    experiences: List<ResumeExperience> = emptyList(),
    onExperiencesChange: (List<ResumeExperience>) -> Unit = {},
    onDraftChange: (ResumeExperience?) -> Unit = {},
    onDraftFinished: () -> Unit = {},
) {
    var editingId by rememberSaveable { mutableStateOf(if (experiences.isEmpty()) NewEntryId else null) }
    var position by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var startYear by rememberSaveable { mutableStateOf("") }
    var endYear by rememberSaveable { mutableStateOf("") }
    var isCurrent by rememberSaveable { mutableStateOf(false) }
    var achievementsSeed by remember { mutableStateOf("") }
    var formattingSeed by remember { mutableStateOf(ResumeTextFormatting()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val achievementsEditor = remember(editingId, achievementsSeed, formattingSeed) {
        RichTextEditorState(achievementsSeed, formattingSeed)
    }

    fun resetEditor() {
        position = ""
        company = ""
        city = ""
        startYear = ""
        endYear = ""
        isCurrent = false
        achievementsSeed = ""
        formattingSeed = ResumeTextFormatting()
        showErrors = false
    }

    fun startNew() {
        onDraftFinished()
        resetEditor()
        editingId = NewEntryId
    }

    fun startEditing(item: ResumeExperience) {
        onDraftFinished()
        position = item.position
        company = item.company
        city = item.city
        startYear = item.startYear
        endYear = item.endYear.takeUnless { item.isOngoing }.orEmpty()
        isCurrent = item.isOngoing
        achievementsSeed = item.achievements
        formattingSeed = item.achievementsFormatting
        showErrors = false
        editingId = item.id
    }

    fun hasPendingInput() = listOf(
        position,
        company,
        city,
        startYear,
        endYear,
        achievementsEditor.value.text,
    ).any(String::isNotBlank) || isCurrent

    val draftEntryId = rememberSaveable(editingId) { newResumeEntryId() }
    if (editingId != null) {
        val pendingEntry = if (hasPendingInput() || editingId != NewEntryId) {
            ResumeExperience(
                position = position,
                company = company,
                city = city,
                startYear = startYear,
                endYear = endYear.takeUnless { isCurrent }.orEmpty(),
                isCurrent = isCurrent,
                achievements = achievementsEditor.value.text,
                achievementsFormatting = achievementsEditor.formatting,
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
        if (position.isBlank() || company.isBlank() ||
            !startYear.isValidMonthYearOrBlank() || (!isCurrent && !endYear.isValidMonthYearOrBlank())
        ) {
            AppAnalytics.error("validate_experience", code = "invalid_fields")
            return false
        }
        val currentId = editingId
        val value = ResumeExperience(
            position = position.trim(),
            company = company.trim(),
            city = city.trim(),
            startYear = startYear.trim(),
            endYear = endYear.trim().takeUnless { isCurrent }.orEmpty(),
            isCurrent = isCurrent,
            achievements = achievementsEditor.value.text.trim(),
            achievementsFormatting = achievementsEditor.formatting,
            id = currentId?.takeUnless { it == NewEntryId } ?: draftEntryId,
        )
        onExperiencesChange(
            if (currentId == NewEntryId) listOf(value) + experiences
            else experiences.replaceById(value.id, ResumeExperience::id, value),
        )
        editingId = null
        resetEditor()
        return true
    }

    fun continueFlow() {
        when {
            editingId == null -> onContinue()
            editingId == NewEntryId && !hasPendingInput() -> {
                editingId = null
                onContinue()
            }
            saveEditor() -> onContinue()
        }
    }

    BuilderPage(
        onBack = onBack,
        step = 2,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_experience_fbb04c),
        title = stringResource(R.string.ui_tell_us_about_your_work_experience_905d16),
        description = stringResource(R.string.ui_add_your_previous_jobs_to_show_your_career_path_20a658),
        actionText = stringResource(R.string.ui_continue_to_education_15dd16),
        onContinue = ::continueFlow,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editingId == NewEntryId) {
                BuilderNewEntryCard(
                    title = stringResource(R.string.ui_new_experience_096118),
                    bringIntoView = experiences.isNotEmpty(),
                ) {
                    Spacer(Modifier.height(16.dp))
                    ExperienceEditor(
                        position = position,
                        onPositionChange = { position = it },
                        company = company,
                        onCompanyChange = { company = it },
                        city = city,
                        onCityChange = { city = it },
                        startYear = startYear,
                        onStartYearChange = { startYear = it.filter { character -> character.isDigit() || character == '/' } },
                        endYear = endYear,
                        onEndYearChange = { endYear = it.filter { character -> character.isDigit() || character == '/' } },
                        isCurrent = isCurrent,
                        onIsCurrentChange = { isCurrent = it },
                        achievementsEditor = achievementsEditor,
                        showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                        onSave = ::saveEditor,
                    )
                }
            }

            experiences.forEach { item ->
                val expanded = editingId == item.id
                BuilderSavedEntryCard(
                    title = item.position,
                    subtitle = item.company,
                    supportingText = listOf(item.city, dateRange(item.startYear, item.localizedEndYear()))
                        .filter(String::isNotBlank)
                        .joinToString(" · "),
                    expanded = expanded,
                    onEdit = {
                        if (expanded) editingId = null else startEditing(item)
                    },
                    onDelete = {
                        onExperiencesChange(experiences.filterNot { it.id == item.id })
                        if (expanded) {
                            editingId = null
                            resetEditor()
                        }
                    },
                    editor = if (expanded) {
                        {
                            ExperienceEditor(
                                position = position,
                                onPositionChange = { position = it },
                                company = company,
                                onCompanyChange = { company = it },
                                city = city,
                                onCityChange = { city = it },
                                startYear = startYear,
                                onStartYearChange = { startYear = it.filter { character -> character.isDigit() || character == '/' } },
                                endYear = endYear,
                                onEndYearChange = { endYear = it.filter { character -> character.isDigit() || character == '/' } },
                                isCurrent = isCurrent,
                                onIsCurrentChange = { isCurrent = it },
                                achievementsEditor = achievementsEditor,
                                showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                                onSave = ::saveEditor,
                            )
                        }
                    } else null,
                )
            }

            if (editingId == null) {
                BuilderAddButton(text = stringResource(R.string.ui_add_job_9b257b), onClick = ::startNew)
            }
        }
    }
}

@Composable
private fun ExperienceEditor(
    position: String,
    onPositionChange: (String) -> Unit,
    company: String,
    onCompanyChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    startYear: String,
    onStartYearChange: (String) -> Unit,
    endYear: String,
    onEndYearChange: (String) -> Unit,
    isCurrent: Boolean,
    onIsCurrentChange: (Boolean) -> Unit,
    achievementsEditor: RichTextEditorState,
    showErrors: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    BuilderTextField(
        value = position,
        onValueChange = onPositionChange,
        label = stringResource(R.string.ui_position_7f57e4),
        placeholder = stringResource(R.string.ui_e_g_android_developer_f3a6a5),
        isError = showErrors && position.isBlank(),
        supportingText = if (showErrors && position.isBlank()) stringResource(R.string.ui_position_is_required_1998db) else null,
    )
    Spacer(Modifier.height(16.dp))
    BuilderTextField(
        value = company,
        onValueChange = onCompanyChange,
        label = stringResource(R.string.ui_company_6eb94b),
        placeholder = stringResource(R.string.ui_company_name_ff4065),
        isError = showErrors && company.isBlank(),
        supportingText = if (showErrors && company.isBlank()) stringResource(R.string.ui_company_is_required_825984) else null,
    )
    Spacer(Modifier.height(16.dp))
    BuilderTextField(city, onCityChange, stringResource(R.string.ui_city_c6fbe2), stringResource(R.string.ui_city_c6fbe2))
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BuilderTextField(
            value = startYear,
            onValueChange = onStartYearChange,
            label = stringResource(R.string.ui_from_966c7c),
            placeholder = stringResource(R.string.ui_month_year_placeholder),
            modifier = Modifier.weight(1f),
            maxLength = 7,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            isError = showErrors && !startYear.isValidMonthYearOrBlank(),
            supportingText = if (showErrors && !startYear.isValidMonthYearOrBlank()) stringResource(R.string.ui_use_month_year) else null,
        )
        BuilderTextField(
            value = if (isCurrent) stringResource(R.string.ui_present_45d9cf) else endYear,
            onValueChange = onEndYearChange,
            label = stringResource(R.string.ui_to_ccdaad),
            placeholder = if (isCurrent) stringResource(R.string.ui_present_45d9cf) else stringResource(R.string.ui_month_year_placeholder),
            modifier = Modifier.weight(1f),
            maxLength = 7,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            isError = showErrors && !isCurrent && !endYear.isValidMonthYearOrBlank(),
            supportingText = if (showErrors && !isCurrent && !endYear.isValidMonthYearOrBlank()) stringResource(R.string.ui_use_month_year) else null,
            enabled = !isCurrent,
        )
    }
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                AppAnalytics.selection("current_job", (!isCurrent).toString())
                onIsCurrentChange(!isCurrent)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isCurrent,
            onCheckedChange = null,
        )
        Text(modifier = Modifier.padding(horizontal = 6.dp), text = stringResource(R.string.ui_i_currently_work_here_c5d8e2))
    }
    Spacer(Modifier.height(16.dp))
    RichTextEditor(
        state = achievementsEditor,
        label = stringResource(R.string.ui_achievements_and_responsibilities_419333),
        placeholder = stringResource(R.string.ui_describe_your_responsibilities_97134a),
        minLines = 3,
    )
    Spacer(Modifier.height(18.dp))
    BuilderEditorActions(onCancel = onCancel, onSave = onSave)
}

private fun dateRange(start: String, end: String): String =
    listOf(start, end).filter(String::isNotBlank).joinToString(" - ")

@Preview(name = "Experience Light", showBackground = true, heightDp = 900)
@Composable
private fun ExperienceLightPreview() {
    CvFreeTheme(darkTheme = false) { ExperienceScreen({}, {}) }
}

@Preview(name = "Experience Dark", showBackground = true, heightDp = 900)
@Composable
private fun ExperienceDarkPreview() {
    CvFreeTheme(darkTheme = true) { ExperienceScreen({}, {}) }
}

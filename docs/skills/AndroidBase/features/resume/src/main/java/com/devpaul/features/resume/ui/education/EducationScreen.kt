package com.devpaul.features.resume.ui.education

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeEducation
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

@Composable
fun EducationScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 5,
    education: List<ResumeEducation> = emptyList(),
    onEducationChange: (List<ResumeEducation>) -> Unit = {},
    onDraftChange: (ResumeEducation?) -> Unit = {},
    onDraftFinished: () -> Unit = {},
) {
    var editingId by rememberSaveable { mutableStateOf(if (education.isEmpty()) NewEntryId else null) }
    var institution by rememberSaveable { mutableStateOf("") }
    var degree by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var startYear by rememberSaveable { mutableStateOf("") }
    var endYear by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun resetEditor() {
        institution = ""
        degree = ""
        city = ""
        startYear = ""
        endYear = ""
        showErrors = false
    }

    fun startNew() {
        onDraftFinished()
        resetEditor()
        editingId = NewEntryId
    }

    fun startEditing(item: ResumeEducation) {
        onDraftFinished()
        institution = item.institution
        degree = item.degree
        city = item.city
        startYear = item.startYear
        endYear = item.endYear
        showErrors = false
        editingId = item.id
    }

    fun hasPendingInput() = listOf(institution, degree, city, startYear, endYear).any(String::isNotBlank)

    val draftEntryId = rememberSaveable(editingId) { newResumeEntryId() }
    if (editingId != null) {
        val pendingEntry = if (hasPendingInput() || editingId != NewEntryId) {
            ResumeEducation(
                institution = institution,
                degree = degree,
                city = city,
                startYear = startYear,
                endYear = endYear,
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
        if (institution.isBlank() || degree.isBlank() ||
            !startYear.isValidMonthYearOrBlank() || !endYear.isValidMonthYearOrBlank()
        ) {
            AppAnalytics.error("validate_education", code = "invalid_fields")
            return false
        }
        val currentId = editingId
        val value = ResumeEducation(
            institution = institution.trim(),
            degree = degree.trim(),
            city = city.trim(),
            startYear = startYear.trim(),
            endYear = endYear.trim(),
            id = currentId?.takeUnless { it == NewEntryId } ?: draftEntryId,
        )
        onEducationChange(
            if (currentId == NewEntryId) listOf(value) + education
            else education.replaceById(value.id, ResumeEducation::id, value),
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
        step = 3,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_education_c77e9b),
        title = stringResource(R.string.ui_add_your_education_5367bc),
        description = stringResource(R.string.ui_include_relevant_studies_programs_or_certifications_4260c0),
        actionText = stringResource(R.string.ui_continue_to_skills_40de13),
        onContinue = ::continueFlow,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editingId == NewEntryId) {
                BuilderNewEntryCard(
                    title = stringResource(R.string.ui_new_education_5a8295),
                    bringIntoView = education.isNotEmpty(),
                ) {
                    Spacer(Modifier.height(16.dp))
                    EducationEditor(
                        institution = institution,
                        onInstitutionChange = { institution = it },
                        degree = degree,
                        onDegreeChange = { degree = it },
                        city = city,
                        onCityChange = { city = it },
                        startYear = startYear,
                        onStartYearChange = { startYear = it.filter { character -> character.isDigit() || character == '/' } },
                        endYear = endYear,
                        onEndYearChange = { endYear = it.filter { character -> character.isDigit() || character == '/' } },
                        showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                        onSave = ::saveEditor,
                    )
                }
            }

            education.forEach { item ->
                val expanded = editingId == item.id
                BuilderSavedEntryCard(
                    title = item.degree,
                    subtitle = item.institution,
                    supportingText = listOf(item.city, dateRange(item.startYear, item.endYear))
                        .filter(String::isNotBlank)
                        .joinToString(" · "),
                    expanded = expanded,
                    onEdit = { if (expanded) editingId = null else startEditing(item) },
                    onDelete = {
                        onEducationChange(education.filterNot { it.id == item.id })
                        if (expanded) {
                            editingId = null
                            resetEditor()
                        }
                    },
                    editor = if (expanded) {
                        {
                            EducationEditor(
                                institution = institution,
                                onInstitutionChange = { institution = it },
                                degree = degree,
                                onDegreeChange = { degree = it },
                                city = city,
                                onCityChange = { city = it },
                                startYear = startYear,
                                onStartYearChange = { startYear = it.filter { character -> character.isDigit() || character == '/' } },
                                endYear = endYear,
                                onEndYearChange = { endYear = it.filter { character -> character.isDigit() || character == '/' } },
                                showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                                onSave = ::saveEditor,
                            )
                        }
                    } else null,
                )
            }

            if (editingId == null) {
                BuilderAddButton(text = stringResource(R.string.ui_add_education_41c1e9), onClick = ::startNew)
            }
        }
    }
}

@Composable
private fun EducationEditor(
    institution: String,
    onInstitutionChange: (String) -> Unit,
    degree: String,
    onDegreeChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    startYear: String,
    onStartYearChange: (String) -> Unit,
    endYear: String,
    onEndYearChange: (String) -> Unit,
    showErrors: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    BuilderTextField(
        value = institution,
        onValueChange = onInstitutionChange,
        label = stringResource(R.string.ui_institution_c342a8),
        placeholder = stringResource(R.string.ui_university_institute_e9c1fb),
        isError = showErrors && institution.isBlank(),
        supportingText = if (showErrors && institution.isBlank()) stringResource(R.string.ui_institution_is_required_02f6dc) else null,
    )
    Spacer(Modifier.height(16.dp))
    BuilderTextField(
        value = degree,
        onValueChange = onDegreeChange,
        label = stringResource(R.string.ui_degree_or_field_of_study_fb4221),
        placeholder = stringResource(R.string.ui_systems_engineering_aaa2c6),
        isError = showErrors && degree.isBlank(),
        supportingText = if (showErrors && degree.isBlank()) stringResource(R.string.ui_degree_or_field_of_study_is_required_6836ef) else null,
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
            value = endYear,
            onValueChange = onEndYearChange,
            label = stringResource(R.string.ui_to_ccdaad),
            placeholder = stringResource(R.string.ui_month_year_placeholder),
            modifier = Modifier.weight(1f),
            maxLength = 7,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            isError = showErrors && !endYear.isValidMonthYearOrBlank(),
            supportingText = if (showErrors && !endYear.isValidMonthYearOrBlank()) stringResource(R.string.ui_use_month_year) else null,
        )
    }
    Spacer(Modifier.height(18.dp))
    BuilderEditorActions(onCancel = onCancel, onSave = onSave)
}

private fun dateRange(start: String, end: String): String =
    listOf(start, end).filter(String::isNotBlank).joinToString(" - ")

@Preview(name = "Education Light", showBackground = true, heightDp = 900)
@Composable
private fun EducationLightPreview() {
    CvFreeTheme(darkTheme = false) { EducationScreen({}, {}) }
}

@Preview(name = "Education Dark", showBackground = true, heightDp = 900)
@Composable
private fun EducationDarkPreview() {
    CvFreeTheme(darkTheme = true) { EducationScreen({}, {}) }
}

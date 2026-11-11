package com.devpaul.features.resume.ui.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeCourse
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
fun CoursesScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    step: Int,
    totalSteps: Int,
    actionText: String,
    modifier: Modifier = Modifier,
    courses: List<ResumeCourse> = emptyList(),
    onCoursesChange: (List<ResumeCourse>) -> Unit = {},
    onDraftChange: (ResumeCourse?) -> Unit = {},
    onDraftFinished: () -> Unit = {},
) {
    var editingId by rememberSaveable { mutableStateOf(if (courses.isEmpty()) NewEntryId else null) }
    var courseName by rememberSaveable { mutableStateOf("") }
    var institution by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun resetEditor() {
        courseName = ""
        institution = ""
        startDate = ""
        endDate = ""
        showErrors = false
    }

    fun startNew() {
        onDraftFinished()
        resetEditor()
        editingId = NewEntryId
    }

    fun startEditing(item: ResumeCourse) {
        onDraftFinished()
        courseName = item.name
        institution = item.institution
        startDate = item.startDate
        endDate = item.endDate
        showErrors = false
        editingId = item.id
    }

    fun hasPendingInput() = listOf(courseName, institution, startDate, endDate).any(String::isNotBlank)

    val draftEntryId = rememberSaveable(editingId) { newResumeEntryId() }
    if (editingId != null) {
        val pendingEntry = if (hasPendingInput() || editingId != NewEntryId) {
            ResumeCourse(
                name = courseName,
                institution = institution,
                startDate = startDate,
                endDate = endDate,
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
        if (courseName.isBlank() || !startDate.isValidMonthYearOrBlank() || !endDate.isValidMonthYearOrBlank()) {
            AppAnalytics.error("validate_course", code = "invalid_fields")
            return false
        }
        val currentId = editingId
        val value = ResumeCourse(
            name = courseName.trim(),
            institution = institution.trim(),
            startDate = startDate.trim(),
            endDate = endDate.trim(),
            id = currentId?.takeUnless { it == NewEntryId } ?: draftEntryId,
        )
        onCoursesChange(
            if (currentId == NewEntryId) listOf(value) + courses
            else courses.replaceById(value.id, ResumeCourse::id, value),
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
        step = step,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_courses_8d7151),
        title = stringResource(R.string.ui_certifications_and_courses_7bdacd),
        description = stringResource(R.string.ui_include_relevant_courses_that_support_your_knowledge_and_ski_1080be),
        actionText = actionText,
        onContinue = ::continueFlow,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editingId == NewEntryId) {
                BuilderNewEntryCard(
                    title = stringResource(R.string.ui_new_course_or_certification_604366),
                    bringIntoView = courses.isNotEmpty(),
                ) {
                    Spacer(Modifier.height(16.dp))
                    CourseEditor(
                        courseName = courseName,
                        onCourseNameChange = { courseName = it },
                        institution = institution,
                        onInstitutionChange = { institution = it },
                        startDate = startDate,
                        onStartDateChange = { startDate = it },
                        endDate = endDate,
                        onEndDateChange = { endDate = it },
                        showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                        onSave = ::saveEditor,
                    )
                }
            }

            courses.forEach { item ->
                val expanded = editingId == item.id
                BuilderSavedEntryCard(
                    title = item.name,
                    subtitle = item.institution,
                    supportingText = dateRange(item.startDate, item.endDate),
                    expanded = expanded,
                    onEdit = { if (expanded) editingId = null else startEditing(item) },
                    onDelete = {
                        onCoursesChange(courses.filterNot { it.id == item.id })
                        if (expanded) {
                            editingId = null
                            resetEditor()
                        }
                    },
                    editor = if (expanded) {
                        {
                            CourseEditor(
                                courseName = courseName,
                                onCourseNameChange = { courseName = it },
                                institution = institution,
                                onInstitutionChange = { institution = it },
                                startDate = startDate,
                                onStartDateChange = { startDate = it },
                                endDate = endDate,
                                onEndDateChange = { endDate = it },
                                showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                                onSave = ::saveEditor,
                            )
                        }
                    } else null,
                )
            }
            if (editingId == null) {
                BuilderAddButton(text = stringResource(R.string.ui_add_course_1b5690), onClick = ::startNew)
            }
        }
    }
}

@Composable
private fun CourseEditor(
    courseName: String,
    onCourseNameChange: (String) -> Unit,
    institution: String,
    onInstitutionChange: (String) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    showErrors: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    BuilderTextField(
        value = courseName,
        onValueChange = onCourseNameChange,
        label = stringResource(R.string.ui_course_or_certification_name_e4f530),
        placeholder = stringResource(R.string.ui_e_g_android_with_jetpack_compose_a66e8b),
        isError = showErrors && courseName.isBlank(),
        supportingText = if (showErrors && courseName.isBlank()) stringResource(R.string.ui_name_is_required_6043d7) else null,
    )
    Spacer(Modifier.height(16.dp))
    BuilderTextField(
        value = institution,
        onValueChange = onInstitutionChange,
        label = stringResource(R.string.ui_institution_or_organizer_644116),
        placeholder = stringResource(R.string.ui_e_g_google_coursera_platzi_5aebb6),
    )
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BuilderTextField(
            value = startDate,
            onValueChange = onStartDateChange,
            label = stringResource(R.string.ui_start_4f70d8),
            placeholder = stringResource(R.string.ui_month_year_placeholder),
            modifier = Modifier.weight(1f),
            maxLength = 7,
            isError = showErrors && !startDate.isValidMonthYearOrBlank(),
            supportingText = if (showErrors && !startDate.isValidMonthYearOrBlank()) stringResource(R.string.ui_use_month_year) else null,
        )
        BuilderTextField(
            value = endDate,
            onValueChange = onEndDateChange,
            label = stringResource(R.string.ui_end_425106),
            placeholder = stringResource(R.string.ui_month_year_placeholder),
            modifier = Modifier.weight(1f),
            maxLength = 7,
            isError = showErrors && !endDate.isValidMonthYearOrBlank(),
            supportingText = if (showErrors && !endDate.isValidMonthYearOrBlank()) stringResource(R.string.ui_use_month_year) else null,
        )
    }
    Spacer(Modifier.height(18.dp))
    BuilderEditorActions(onCancel = onCancel, onSave = onSave)
}

private fun dateRange(start: String, end: String): String =
    listOf(start, end).filter(String::isNotBlank).joinToString(" - ")

@Preview(name = "Courses Light", showBackground = true, heightDp = 880)
@Composable
private fun CoursesLightPreview() {
    CvFreeTheme(darkTheme = false) { CoursesScreen({}, {}, 5, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

@Preview(name = "Courses Dark", showBackground = true, heightDp = 880)
@Composable
private fun CoursesDarkPreview() {
    CvFreeTheme(darkTheme = true) { CoursesScreen({}, {}, 5, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

package com.devpaul.features.resume.ui.references

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.devpaul.core.domain.resume.ResumeReference
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
import com.devpaul.features.resume.ui.components.isValidEmailOrBlank
import com.devpaul.features.resume.ui.components.replaceById

@Composable
fun ReferencesScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    step: Int,
    totalSteps: Int,
    actionText: String,
    modifier: Modifier = Modifier,
    references: List<ResumeReference> = emptyList(),
    onReferencesChange: (List<ResumeReference>) -> Unit = {},
    onDraftChange: (ResumeReference?) -> Unit = {},
    onDraftFinished: () -> Unit = {},
) {
    var editingId by rememberSaveable { mutableStateOf(if (references.isEmpty()) NewEntryId else null) }
    var name by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun resetEditor() {
        name = ""
        company = ""
        phone = ""
        email = ""
        showErrors = false
    }

    fun startNew() {
        onDraftFinished()
        resetEditor()
        editingId = NewEntryId
    }

    fun startEditing(item: ResumeReference) {
        onDraftFinished()
        name = item.name
        company = item.company
        phone = item.phone
        email = item.email
        showErrors = false
        editingId = item.id
    }

    fun hasPendingInput() = listOf(name, company, phone, email).any(String::isNotBlank)

    val draftEntryId = rememberSaveable(editingId) { newResumeEntryId() }
    if (editingId != null) {
        val pendingEntry = if (hasPendingInput() || editingId != NewEntryId) {
            ResumeReference(
                name = name,
                company = company,
                phone = phone,
                email = email,
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
        if (name.isBlank() || (phone.isBlank() && email.isBlank()) || !email.isValidEmailOrBlank()) {
            AppAnalytics.error("validate_reference", code = "invalid_fields")
            return false
        }

        val currentId = editingId
        val value = ResumeReference(
            name = name.trim(),
            company = company.trim(),
            phone = phone.trim(),
            email = email.trim(),
            id = currentId?.takeUnless { it == NewEntryId } ?: draftEntryId,
        )
        onReferencesChange(
            if (currentId == NewEntryId) listOf(value) + references
            else references.replaceById(value.id, ResumeReference::id, value),
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
        stepName = stringResource(R.string.ui_references_382921),
        title = stringResource(R.string.ui_professional_references_e0c09b),
        description = stringResource(R.string.ui_add_people_who_can_vouch_for_your_experience_and_professiona_523732),
        actionText = actionText,
        onContinue = ::continueFlow,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editingId == NewEntryId) {
                BuilderNewEntryCard(
                    title = stringResource(R.string.ui_new_reference_c614de),
                    bringIntoView = references.isNotEmpty(),
                ) {
                    Spacer(Modifier.height(16.dp))
                    ReferenceEditor(
                        name = name,
                        onNameChange = { name = it },
                        company = company,
                        onCompanyChange = { company = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        email = email,
                        onEmailChange = { email = it },
                        showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                        onSave = ::saveEditor,
                    )
                }
            }

            references.forEach { item ->
                val expanded = editingId == item.id
                BuilderSavedEntryCard(
                    title = item.name,
                    subtitle = item.company,
                    supportingText = listOf(item.phone, item.email).filter(String::isNotBlank).joinToString(" · "),
                    expanded = expanded,
                    onEdit = { if (expanded) editingId = null else startEditing(item) },
                    onDelete = {
                        onReferencesChange(references.filterNot { it.id == item.id })
                        if (expanded) {
                            editingId = null
                            resetEditor()
                        }
                    },
                    editor = if (expanded) {
                        {
                            ReferenceEditor(
                                name = name,
                                onNameChange = { name = it },
                                company = company,
                                onCompanyChange = { company = it },
                                phone = phone,
                                onPhoneChange = { phone = it },
                                email = email,
                                onEmailChange = { email = it },
                                showErrors = showErrors,
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                                onSave = ::saveEditor,
                            )
                        }
                    } else null,
                )
            }
            if (editingId == null) {
                BuilderAddButton(text = stringResource(R.string.ui_add_reference_bc2cfa), onClick = ::startNew)
            }
        }
    }
}

@Composable
private fun ReferenceEditor(
    name: String,
    onNameChange: (String) -> Unit,
    company: String,
    onCompanyChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    showErrors: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    BuilderTextField(
        value = name,
        onValueChange = onNameChange,
        label = stringResource(R.string.ui_full_name_cccece),
        placeholder = stringResource(R.string.ui_e_g_andrea_ramirez_b06485),
        isError = showErrors && name.isBlank(),
        supportingText = if (showErrors && name.isBlank()) stringResource(R.string.ui_name_is_required_6043d7) else null,
    )
    Spacer(Modifier.height(16.dp))
    BuilderTextField(
        value = company,
        onValueChange = onCompanyChange,
        label = stringResource(R.string.ui_company_and_position_9198ec),
        placeholder = stringResource(R.string.ui_e_g_andean_technology_engineering_manager_f52628),
    )
    Spacer(Modifier.height(16.dp))
    val missingContact = showErrors && phone.isBlank() && email.isBlank()
    BuilderTextField(
        value = phone,
        onValueChange = onPhoneChange,
        label = stringResource(R.string.ui_phone_99af37),
        placeholder = "+51 999 999 999",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        isError = missingContact,
        supportingText = if (missingContact) stringResource(R.string.ui_add_a_phone_number_or_email_3fa668) else null,
    )
    Spacer(Modifier.height(16.dp))
    val invalidEmail = showErrors && !email.isValidEmailOrBlank()
    BuilderTextField(
        value = email,
        onValueChange = onEmailChange,
        label = stringResource(R.string.ui_email_59a167),
        placeholder = "email@example.com",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        isError = invalidEmail || missingContact,
        supportingText = when {
            invalidEmail -> stringResource(R.string.ui_enter_a_valid_email_d6ab4c)
            missingContact -> stringResource(R.string.ui_add_a_phone_number_or_email_3fa668)
            else -> null
        },
    )
    Spacer(Modifier.height(18.dp))
    BuilderEditorActions(onCancel = onCancel, onSave = onSave)
}

@Preview(name = "References Light", showBackground = true, heightDp = 920)
@Composable
private fun ReferencesLightPreview() {
    CvFreeTheme(darkTheme = false) { ReferencesScreen({}, {}, 8, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

@Preview(name = "References Dark", showBackground = true, heightDp = 920)
@Composable
private fun ReferencesDarkPreview() {
    CvFreeTheme(darkTheme = true) { ReferencesScreen({}, {}, 8, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

package com.devpaul.features.resume.ui.links

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeLink
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
import com.devpaul.features.resume.ui.components.isValidWebUrl
import com.devpaul.features.resume.ui.components.replaceById

@Composable
fun LinksScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    step: Int,
    totalSteps: Int,
    actionText: String,
    modifier: Modifier = Modifier,
    links: List<ResumeLink> = emptyList(),
    onLinksChange: (List<ResumeLink>) -> Unit = {},
    onDraftChange: (ResumeLink?) -> Unit = {},
    onDraftFinished: () -> Unit = {},
) {
    var editingId by rememberSaveable { mutableStateOf(if (links.isEmpty()) NewEntryId else null) }
    var linkName by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun resetEditor() {
        linkName = ""
        url = ""
        showErrors = false
    }

    fun startNew() {
        onDraftFinished()
        resetEditor()
        editingId = NewEntryId
    }

    fun startEditing(item: ResumeLink) {
        onDraftFinished()
        linkName = item.name
        url = item.url
        showErrors = false
        editingId = item.id
    }

    fun hasPendingInput() = linkName.isNotBlank() || url.isNotBlank()

    val draftEntryId = rememberSaveable(editingId) { newResumeEntryId() }
    if (editingId != null) {
        val pendingEntry = if (hasPendingInput() || editingId != NewEntryId) {
            ResumeLink(
                name = linkName,
                url = url,
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
        val duplicate = links.any { it.id != editingId && it.url.equals(url.trim(), ignoreCase = true) }
        if (linkName.isBlank() || !url.isValidWebUrl() || duplicate) {
            AppAnalytics.error("validate_link", code = if (duplicate) "duplicate_link" else "invalid_fields")
            return false
        }

        val currentId = editingId
        val value = ResumeLink(
            name = linkName.trim(),
            url = url.trim(),
            id = currentId?.takeUnless { it == NewEntryId } ?: draftEntryId,
        )
        onLinksChange(
            if (currentId == NewEntryId) listOf(value) + links
            else links.replaceById(value.id, ResumeLink::id, value),
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
        stepName = stringResource(R.string.ui_links_3029a5),
        title = stringResource(R.string.ui_links_and_social_profiles_927b08),
        description = stringResource(R.string.ui_add_professional_profiles_to_better_showcase_your_work_and_e_953589),
        actionText = actionText,
        onContinue = ::continueFlow,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editingId == NewEntryId) {
                BuilderNewEntryCard(
                    title = stringResource(R.string.ui_new_link_da7b3f),
                    bringIntoView = links.isNotEmpty(),
                ) {
                    Spacer(Modifier.height(16.dp))
                    LinkEditor(
                        linkName = linkName,
                        onLinkNameChange = { linkName = it },
                        url = url,
                        onUrlChange = { url = it },
                        showErrors = showErrors,
                        duplicateUrl = links.any { it.url.equals(url.trim(), ignoreCase = true) },
                        onPreset = { name, presetUrl ->
                            linkName = name
                            url = presetUrl
                        },
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                        onSave = ::saveEditor,
                    )
                }
            }

            links.forEach { item ->
                val expanded = editingId == item.id
                BuilderSavedEntryCard(
                    title = item.name,
                    subtitle = item.url,
                    expanded = expanded,
                    onEdit = { if (expanded) editingId = null else startEditing(item) },
                    onDelete = {
                        onLinksChange(links.filterNot { it.id == item.id })
                        if (expanded) {
                            editingId = null
                            resetEditor()
                        }
                    },
                    editor = if (expanded) {
                        {
                            LinkEditor(
                                linkName = linkName,
                                onLinkNameChange = { linkName = it },
                                url = url,
                                onUrlChange = { url = it },
                                showErrors = showErrors,
                                duplicateUrl = links.any {
                                    it.id != editingId && it.url.equals(url.trim(), ignoreCase = true)
                                },
                                onPreset = { name, presetUrl ->
                                    linkName = name
                                    url = presetUrl
                                },
                        onCancel = { onDraftChange(null); editingId = null; resetEditor() },
                                onSave = ::saveEditor,
                            )
                        }
                    } else null,
                )
            }
            if (editingId == null) {
                BuilderAddButton(text = stringResource(R.string.ui_add_link_727982), onClick = ::startNew)
            }
        }
    }
}

@Composable
private fun LinkEditor(
    linkName: String,
    onLinkNameChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    showErrors: Boolean,
    duplicateUrl: Boolean,
    onPreset: (String, String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinkPreset.entries.forEach { preset ->
            val presetName = stringResource(preset.label)
            OutlinedButton(
                onClick = trackedClick("select_link_preset") { onPreset(presetName, preset.url) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            ) {
                Text(presetName)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    BuilderTextField(
        value = linkName,
        onValueChange = onLinkNameChange,
        label = stringResource(R.string.ui_link_name_ad3f67),
        placeholder = stringResource(R.string.ui_e_g_linkedin_9f2fa3),
        isError = showErrors && linkName.isBlank(),
        supportingText = if (showErrors && linkName.isBlank()) stringResource(R.string.ui_name_is_required_6043d7) else null,
    )
    Spacer(Modifier.height(16.dp))
    val urlError = when {
        duplicateUrl -> stringResource(R.string.ui_this_link_has_already_been_added_532ee0)
        url.isNotBlank() && !url.isValidWebUrl() -> stringResource(R.string.ui_enter_a_complete_url_beginning_with_https_8eb976)
        showErrors && url.isBlank() -> stringResource(R.string.ui_url_is_required_d73713)
        else -> null
    }
    BuilderTextField(
        value = url,
        onValueChange = onUrlChange,
        label = "URL",
        placeholder = "https://",
        isError = showErrors && urlError != null,
        supportingText = if (showErrors) urlError else null,
    )
    Spacer(Modifier.height(18.dp))
    BuilderEditorActions(onCancel = onCancel, onSave = onSave)
}

@Preview(name = "Links Light", showBackground = true, heightDp = 850)
@Composable
private fun LinksLightPreview() {
    CvFreeTheme(darkTheme = false) { LinksScreen({}, {}, 6, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

@Preview(name = "Links Dark", showBackground = true, heightDp = 850)
@Composable
private fun LinksDarkPreview() {
    CvFreeTheme(darkTheme = true) { LinksScreen({}, {}, 6, 9, stringResource(R.string.ui_continue_4ad2b3)) }
}

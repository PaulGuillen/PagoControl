package com.devpaul.features.resume.ui.skills

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.ui.components.BuilderFormCard
import com.devpaul.features.resume.ui.components.BuilderPage
import com.devpaul.features.resume.ui.components.TrackResumeDraft

private const val MaxSkillLength = 40
@Composable
fun SkillsScreen(
    onBack: () -> Unit,
    onContinue: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 5,
    actionText: String = stringResource(R.string.ui_continue_to_professional_summary_c7a0cf),
    initialSkills: List<String> = listOf("Kotlin", "Jetpack Compose"),
    onDraftChange: (List<String>) -> Unit = {},
) {
    val suggestedSkills = listOf("Docker", "GraphQL", stringResource(R.string.ui_agile_methodologies_cc0fae), "Git", "Figma")
    var input by rememberSaveable { mutableStateOf("") }
    var skills by rememberSaveable(initialSkills) { mutableStateOf(initialSkills.distinct()) }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }

    val pendingSkill = input.trim()
    val draftSkills = if (pendingSkill.isNotEmpty() && skills.none { it.equals(pendingSkill, ignoreCase = true) }) {
        skills + pendingSkill
    } else {
        skills
    }
    TrackResumeDraft(draftSkills, onDraftChange)

    val skillLengthError = stringResource(R.string.skill_max_length, MaxSkillLength)
    val duplicateSkillError = stringResource(R.string.ui_this_skill_has_already_been_added_340179)

    fun skillsWithPendingInput(): List<String>? {
        val skill = input.trim()
        if (skill.isEmpty()) return skills
        inputError = when {
            skill.length > MaxSkillLength -> skillLengthError
            skills.any { it.equals(skill, ignoreCase = true) } -> duplicateSkillError
            else -> null
        }
        if (inputError != null) AppAnalytics.error("validate_skill", code = "invalid_or_duplicate_skill")
        return if (inputError == null) listOf(skill) + skills else null
    }

    fun addPendingSkill() {
        skillsWithPendingInput()?.let { updated ->
            skills = updated
            input = ""
            inputError = null
        }
    }

    BuilderPage(
        onBack = onBack,
        step = 4,
        totalSteps = totalSteps,
        stepName = stringResource(R.string.ui_skills_bff903),
        title = stringResource(R.string.ui_showcase_your_skills_6173cd),
        description = stringResource(R.string.ui_add_technical_skills_and_tools_that_define_your_profile_27b9a1),
        actionText = actionText,
        onContinue = {
            skillsWithPendingInput()?.let { completedSkills ->
                skills = completedSkills
                input = ""
                onContinue(completedSkills)
            }
        },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        inputError = null
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.ui_search_or_add_a_skill_d933b8)) },
                    singleLine = true,
                    isError = inputError != null,
                    supportingText = inputError?.let { message -> { Text(message) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addPendingSkill() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = trackedClick("add_skill", ::addPendingSkill),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.ui_add_41261d))
                }
            }
        }
        if (skills.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                skills.forEach { skill ->
                    SelectedSkillChip(skill) {
                        skills = skills.filterNot { it == skill }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        BuilderFormCard {
            Text(
                text = stringResource(R.string.ui_suggestions_for_you_bf6ab6),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestedSkills.filterNot { suggestion ->
                    skills.any { it.equals(suggestion, ignoreCase = true) }
                }.forEach { skill ->
                    SuggestedSkillChip(skill) { skills = listOf(skill) + skills }
                }
            }
        }
    }
}

@Composable
private fun SelectedSkillChip(label: String, onRemove: () -> Unit) {
    Surface(
        onClick = trackedClick("remove_skill", onRemove),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 9.dp, end = 10.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.remove_skill, label),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SuggestedSkillChip(label: String, onAdd: () -> Unit) {
    Surface(
        onClick = trackedClick("add_suggested_skill", onAdd),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("+ $label", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Preview(name = "Skills Light", showBackground = true, heightDp = 820)
@Composable
private fun SkillsLightPreview() {
    CvFreeTheme(darkTheme = false) { SkillsScreen({}, {}) }
}

@Preview(name = "Skills Dark", showBackground = true, heightDp = 820)
@Composable
private fun SkillsDarkPreview() {
    CvFreeTheme(darkTheme = true) { SkillsScreen({}, {}) }
}

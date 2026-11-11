package com.devpaul.features.resume.ui.sections

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.ui.components.TrackResumeDraft
import com.devpaul.features.resume.ui.components.BuilderTopBar

@Composable
fun SectionSelectionScreen(
    onBack: () -> Unit,
    onConfirm: (Set<ResumeOptionalSection>) -> Unit,
    modifier: Modifier = Modifier,
    initialSections: Set<ResumeOptionalSection> = emptySet(),
    onDraftChange: (Set<ResumeOptionalSection>) -> Unit = {},
) {
    var courses by rememberSaveable(initialSections) {
        mutableStateOf(ResumeOptionalSection.Courses in initialSections)
    }
    var links by rememberSaveable(initialSections) {
        mutableStateOf(ResumeOptionalSection.Links in initialSections)
    }
    var languages by rememberSaveable(initialSections) {
        mutableStateOf(ResumeOptionalSection.Languages in initialSections)
    }
    var references by rememberSaveable(initialSections) {
        mutableStateOf(ResumeOptionalSection.References in initialSections)
    }
    TrackResumeDraft(
        buildSet {
            if (courses) add(ResumeOptionalSection.Courses)
            if (links) add(ResumeOptionalSection.Links)
            if (languages) add(ResumeOptionalSection.Languages)
            if (references) add(ResumeOptionalSection.References)
        },
        onDraftChange,
    )
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        BuilderTopBar(onBack = onBack, title = stringResource(R.string.ui_which_sections_do_you_want_to_include_94036c))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(stringResource(R.string.ui_main_sections_954587))
            Spacer(modifier = Modifier.height(12.dp))
            SectionRow(Icons.Outlined.Contacts, stringResource(R.string.ui_contact_information_d4ed96), true, {}, true)
            SectionRow(Icons.Outlined.WorkOutline, stringResource(R.string.ui_work_experience_b6b2aa), true, {}, true)
            SectionRow(Icons.Outlined.School, stringResource(R.string.ui_education_c77e9b), true, {}, true)
            SectionRow(Icons.Outlined.Star, stringResource(R.string.ui_skills_bff903), true, {}, true)
            SectionRow(Icons.Outlined.EditNote, stringResource(R.string.ui_professional_summary_b65e50), true, {}, true)
            Spacer(modifier = Modifier.height(28.dp))
            SectionTitle(stringResource(R.string.ui_optional_sections_9663d0))
            Spacer(modifier = Modifier.height(12.dp))
            SectionRow(Icons.AutoMirrored.Outlined.MenuBook, stringResource(R.string.ui_courses_8d7151), courses, { courses = it })
            SectionRow(Icons.Outlined.Link, stringResource(R.string.ui_links_3029a5), links, { links = it })
            SectionRow(Icons.Outlined.Language, stringResource(R.string.ui_languages_553733), languages, { languages = it })
            SectionRow(Icons.Outlined.Contacts, stringResource(R.string.ui_references_382921), references, { references = it })
            Spacer(modifier = Modifier.height(20.dp))
        }
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = trackedClick("restore_sections") {
                    courses = ResumeOptionalSection.Courses in initialSections
                    links = ResumeOptionalSection.Links in initialSections
                    languages = ResumeOptionalSection.Languages in initialSections
                    references = ResumeOptionalSection.References in initialSections
                },
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.ui_restore_f9cdc1), style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = trackedClick("confirm_sections") {
                    onConfirm(
                        buildSet {
                            if (courses) add(ResumeOptionalSection.Courses)
                            if (links) add(ResumeOptionalSection.Links)
                            if (languages) add(ResumeOptionalSection.Languages)
                            if (references) add(ResumeOptionalSection.References)
                        },
                    )
                },
                modifier = Modifier
                    .weight(1.35f)
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(stringResource(R.string.ui_confirm_81b4b6), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
private fun SectionRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    required: Boolean = false,
) {
    Surface(
        onClick = trackedClick("toggle_section") {
            if (!required) {
                AppAnalytics.selection("section_${icon.name.substringAfterLast('.').lowercase()}", (!checked).toString())
                onCheckedChange(!checked)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Checkbox(
                checked = checked,
                onCheckedChange = if (required) null else { checked ->
                    AppAnalytics.selection("section_${icon.name.substringAfterLast('.').lowercase()}", checked.toString())
                    onCheckedChange(checked)
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Preview(name = "Sections Light", showBackground = true, heightDp = 1200)
@Composable
private fun SectionsLightPreview() {
    CvFreeTheme(darkTheme = false) { SectionSelectionScreen({}, {}) }
}

@Preview(name = "Sections Dark", showBackground = true, heightDp = 1200)
@Composable
private fun SectionsDarkPreview() {
    CvFreeTheme(darkTheme = true) { SectionSelectionScreen({}, {}) }
}

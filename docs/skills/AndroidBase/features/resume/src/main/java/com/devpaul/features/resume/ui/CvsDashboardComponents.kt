package com.devpaul.features.resume.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.designsystem.component.CvAppHeader
import com.devpaul.core.designsystem.component.CvHeaderAction
import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.resume.R

internal val CvsNavy = Color(0xFF0B192C)
internal val CvsSapphire = Color(0xFF1E3A8A)
internal val CvsBlue = Color(0xFF2563EB)

@Composable
internal fun CvsHeader(
    profile: UserProfileUiState,
    isPremium: Boolean,
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
) {
    CvAppHeader(profile = profile, isPremium = isPremium) {
        CvHeaderAction(Icons.Outlined.Search, stringResource(R.string.cvs_search), onSearch)
        CvHeaderAction(Icons.Outlined.Notifications, stringResource(R.string.cvs_notifications), onNotifications)
    }
}

@Composable
internal fun CvsViewToggle(gridView: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(Modifier.padding(3.dp)) {
            listOf(false, true).forEach { grid ->
                IconButton(
                    onClick = { onChange(grid) },
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (grid == gridView) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .semantics { selected = grid == gridView },
                ) {
                    Icon(
                        if (grid) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
                        stringResource(if (grid) R.string.cvs_grid_view else R.string.cvs_list_view),
                        Modifier.size(16.dp),
                        tint = if (grid == gridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CvsProfileStrength(resume: ResumeDocument, compact: Boolean) {
    var showAnalysis by remember { mutableStateOf(false) }
    val sections = listOf(
        R.string.cvs_contact to (resume.contact.name.isNotBlank() && resume.contact.professionalTitle.isNotBlank() &&
            (resume.contact.email.isNotBlank() || resume.contact.phone.isNotBlank())),
        R.string.cvs_experience to resume.experiences.any { it.position.isNotBlank() && it.company.isNotBlank() },
        R.string.cvs_education to resume.education.any { it.institution.isNotBlank() && it.degree.isNotBlank() },
        R.string.cvs_skills to resume.skills.any(String::isNotBlank),
        R.string.cvs_summary to resume.summary.isNotBlank(),
    )
    val percent = sections.count { it.second } * 100 / sections.size
    val success = if (LocalAppDarkTheme.current) Color(0xFF6EE7B7) else Color(0xFF047857)
    if (showAnalysis) {
        AlertDialog(
            onDismissRequest = { showAnalysis = false },
            title = { Text(stringResource(R.string.cvs_profile_strength)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.cvs_analysis_description))
                    sections.forEach { (label, complete) ->
                        Text(stringResource(
                            if (complete) R.string.cvs_section_complete else R.string.cvs_section_missing,
                            stringResource(label),
                        ))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnalysis = false }) { Text(stringResource(R.string.cvs_close)) }
            },
        )
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.cvs_profile_strength),
                    Modifier.weight(1f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("$percent%", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = CvsBlue,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            if (!compact) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (percent == 100) {
                        Icon(Icons.Outlined.CheckCircle, null, Modifier.size(14.dp), tint = success)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        stringResource(if (percent == 100) R.string.cvs_profile_complete else R.string.cvs_profile_pending),
                        Modifier.weight(1f),
                        color = if (percent == 100) success else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                    TextButton(
                        onClick = { showAnalysis = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Text(stringResource(R.string.cvs_analysis), fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CvsQuickActions(enabled: Boolean, onDuplicate: () -> Unit, onTemplate: () -> Unit, resumeName: String?) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Bolt, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.cvs_quick_actions), fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                if (resumeName == null) stringResource(R.string.cvs_choose_action_hint)
                else stringResource(R.string.cvs_actions_for, resumeName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CvsQuickAction(
                    icon = Icons.Outlined.ContentCopy,
                    title = stringResource(R.string.cvs_duplicate),
                    description = stringResource(R.string.cvs_duplicate_description),
                    enabled = enabled,
                    onClick = trackedClick("duplicate_resume", onDuplicate),
                    modifier = Modifier.weight(1f),
                )
                CvsQuickAction(
                    icon = Icons.Outlined.Dashboard,
                    title = stringResource(R.string.cvs_change_template),
                    description = stringResource(R.string.cvs_template_description),
                    enabled = true,
                    onClick = trackedClick("change_resume_template", onTemplate),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CvsQuickAction(icon: ImageVector, title: String, description: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 10.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

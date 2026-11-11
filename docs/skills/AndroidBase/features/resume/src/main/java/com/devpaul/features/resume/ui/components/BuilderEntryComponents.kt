package com.devpaul.features.resume.ui.components

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.TrackAnalyticsView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devpaul.features.resume.R

@Composable
fun BuilderSavedEntryCard(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    editor: @Composable (ColumnScope.() -> Unit)? = null,
) {
    var deleteConfirmationVisible by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (expanded) 2.dp else 1.dp,
            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = trackedClick("edit_entry", onEdit))
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { stringResource(R.string.ui_untitled_8827eb) },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!expanded && !supportingText.isNullOrBlank()) {
                        Text(
                            text = supportingText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = trackedClick("edit_entry", onEdit)) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.Edit,
                        contentDescription = (if (expanded) stringResource(R.string.ui_collapse_29bd1f) else stringResource(R.string.ui_edit_2b5f23)),
                    )
                }
                IconButton(onClick = trackedClick("request_delete_entry") { deleteConfirmationVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.ui_delete_54e140),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            AnimatedVisibility(visible = expanded && editor != null) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    content = editor ?: {},
                )
            }
        }
    }

    if (deleteConfirmationVisible) {
        TrackAnalyticsView("delete_entry_dialog")
        AlertDialog(
            onDismissRequest = { deleteConfirmationVisible = false },
            title = { Text(stringResource(R.string.ui_delete_this_item_a1cd1e)) },
            text = { Text(stringResource(R.string.ui_this_action_will_remove_the_item_from_the_resume_38cf20)) },
            confirmButton = {
                TextButton(
                    onClick = trackedClick("confirm_delete_entry") {
                        deleteConfirmationVisible = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(R.string.ui_delete_54e140), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = trackedClick("cancel_delete_entry") { deleteConfirmationVisible = false }) {
                    Text(stringResource(R.string.ui_cancel_c111e0))
                }
            },
        )
    }
}

@Composable
fun BuilderNewEntryCard(
    title: String,
    modifier: Modifier = Modifier,
    bringIntoView: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(bringIntoView) {
        if (bringIntoView) bringIntoViewRequester.bringIntoView()
    }

    Surface(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            content()
        }
    }
}

@Composable
fun BuilderEditorActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = trackedClick("cancel_entry", onCancel)) {
            Text(stringResource(R.string.ui_cancel_c111e0))
        }
        Spacer(Modifier.width(10.dp))
        Button(onClick = trackedClick("save_entry", onSave), enabled = saveEnabled) {
            Text(stringResource(R.string.ui_save_a0b626))
        }
    }
}

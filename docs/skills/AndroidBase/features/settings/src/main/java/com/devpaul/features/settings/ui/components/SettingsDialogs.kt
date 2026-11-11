package com.devpaul.features.settings.ui.components

import com.devpaul.core.platform.analytics.trackedClick
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.features.settings.R
import com.devpaul.features.settings.ui.util.labelRes

@Composable
internal fun EditNameDialog(
    name: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.settings_edit_name)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_name)) },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { message -> { Text(message) } },
            )
        },
        confirmButton = {
            Button(onClick = trackedClick("save_profile_name", onSave), enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    stringResource(
                        if (isSaving) R.string.settings_saving else R.string.settings_save,
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = trackedClick("cancel_profile_name", onDismiss), enabled = !isSaving) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@Composable
internal fun LanguageSelectionDialog(
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_choose_language)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = language == selectedLanguage,
                                role = Role.RadioButton,
                                onClick = trackedClick("select_language") { onLanguageSelected(language) },
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = language == selectedLanguage, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(language.labelRes()), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
    )
}

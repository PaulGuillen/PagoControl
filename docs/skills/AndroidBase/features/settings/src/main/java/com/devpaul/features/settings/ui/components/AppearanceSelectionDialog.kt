package com.devpaul.features.settings.ui.components

import com.devpaul.core.platform.analytics.trackedClick
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.devpaul.core.platform.theme.AppAppearance
import com.devpaul.features.settings.R
import com.devpaul.features.settings.ui.util.labelRes

@Composable
internal fun AppearanceSelectionDialog(
    selectedAppearance: AppAppearance,
    onDismiss: () -> Unit,
    onAppearanceSelected: (AppAppearance) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_choose_appearance)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                AppAppearance.entries.forEach { appearance ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = appearance == selectedAppearance,
                                role = Role.RadioButton,
                                onClick = trackedClick("select_appearance") { onAppearanceSelected(appearance) },
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = appearance == selectedAppearance, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(appearance.labelRes()), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
    )
}

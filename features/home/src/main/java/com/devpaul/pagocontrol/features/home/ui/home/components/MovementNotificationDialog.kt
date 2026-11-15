package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.features.home.domain.entity.Movement

@Composable
internal fun MovementNotificationDialog(movement: Movement, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.movement_notification_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MovementAvatar(movement.source, movement.packageName)
                    Column {
                        Text(movement.source, style = MaterialTheme.typography.titleSmall)
                        Text(movement.detail, style = MaterialTheme.typography.bodySmall)
                    }
                }
                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (movement.notificationTitle.isNotBlank()) {
                            Text(movement.notificationTitle, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            movement.notificationText.ifBlank {
                                stringResource(R.string.movement_notification_empty)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.movement_notification_close))
            }
        },
    )
}

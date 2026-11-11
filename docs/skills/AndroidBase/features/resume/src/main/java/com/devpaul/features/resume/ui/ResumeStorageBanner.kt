package com.devpaul.features.resume.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeStorageState
import com.devpaul.features.resume.R

@Composable
internal fun ResumeStorageBanner(
    state: ResumeStorageState,
    onRetry: () -> Unit,
    onMigrate: () -> Unit,
) {
    val message = when {
        state.isMigrating -> R.string.resume_cloud_migrating
        state.hasError -> R.string.resume_cloud_error
        state.isLoading -> R.string.resume_cloud_loading
        state.hasPendingWrites -> R.string.resume_cloud_pending
        state.isFromCache -> R.string.resume_cloud_cached
        else -> R.string.resume_cloud_synced
    }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (state.hasError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(stringResource(message), style = MaterialTheme.typography.bodySmall)
            if (state.hasError && !state.isMigrating) {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.resume_cloud_retry)) }
            }
            if (state.hasLocalResumes && !state.isMigrating) {
                TextButton(onClick = onMigrate) { Text(stringResource(R.string.resume_cloud_import_local)) }
            }
        }
    }
}

package com.example.androidbase.features.home.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbase.domain.AppFailure
import com.example.androidbase.features.home.R
import com.example.androidbase.session.SessionUser
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HomeScreen(user: SessionUser, darkTheme: Boolean, onSignOut: () -> Unit) {
    val viewModel: HomeViewModel = koinViewModel(key = user.id, parameters = { parametersOf(user.id) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state, user.isDemo, darkTheme, onSignOut, viewModel::accept)
}

@Composable
fun HomeContent(
    state: HomeUiState,
    isDemo: Boolean,
    darkTheme: Boolean,
    onSignOut: () -> Unit,
    onIntent: (HomeUiIntent) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.savedCount) { if (state.savedCount > 0) title = "" }
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(if (isDemo) R.string.home_demo else R.string.home_cloud))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().toggleable(
                    value = darkTheme,
                    role = Role.Switch,
                    onValueChange = { onIntent(HomeUiIntent.SetDarkTheme(it)) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.home_dark_theme), modifier = Modifier.weight(1f))
                Switch(checked = darkTheme, onCheckedChange = null)
            }
        }
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.home_item_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isSaving,
                isError = state.failure == AppFailure.INVALID_INPUT,
            )
            Button(
                onClick = { onIntent(HomeUiIntent.Add(title)) },
                enabled = !state.isSaving && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.home_add)) }
        }
        if (state.isLoading || state.isSaving) item { CircularProgressIndicator() }
        if (state.failure != null) item {
            Column {
                Text(
                    stringResource(if (state.failure == AppFailure.INVALID_INPUT) R.string.home_invalid else R.string.home_error),
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = { onIntent(HomeUiIntent.Retry) }) { Text(stringResource(R.string.home_retry)) }
            }
        }
        if (!state.isLoading && state.items.isEmpty()) item { Text(stringResource(R.string.home_empty)) }
        items(state.items, key = { it.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(item.title, modifier = Modifier.padding(16.dp))
            }
        }
        item {
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_sign_out))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

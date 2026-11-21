package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.features.reports.R

@Composable
internal fun ExpenseCategory.displayName(): String = customName ?: stringResource(label())

@Composable
internal fun ReportCategoryManager(state: ReportsUiState, onIntent: (ReportsUiIntent) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    val categories = ExpenseCategory.entries + state.settings.customCategories
    val names = categories.map { it.displayName() }
    val clean = name.trim().replace(Regex("\\s+"), " ")
    val valid = clean.length in 2..40 && names.none { it.equals(clean, true) }
    // Close only when the new category is observed in persistent settings.
    LaunchedEffect(state.settings.customCategories) {
        if (state.settings.customCategories.any { it.customName.equals(clean, true) }) {
            open = false
            name = ""
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.reports_manage_categories), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { open = true }, enabled = !state.loading && !state.error && !state.saving) {
            Text(stringResource(R.string.reports_add_category))
        }
    }
    if (open) AlertDialog(
        onDismissRequest = { if (!state.saving) open = false },
        title = { Text(stringResource(R.string.reports_add_category)) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(names.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(name, { name = it.take(40) }, label = { Text(stringResource(R.string.reports_category_name)) }, singleLine = true, enabled = !state.saving)
                if (name.isNotEmpty() && !valid) Text(stringResource(R.string.reports_category_invalid))
            }
        },
        confirmButton = { TextButton(onClick = { onIntent(ReportsUiIntent.AddCategory(clean)) }, enabled = valid && !state.saving) { Text(stringResource(R.string.reports_save)) } },
        dismissButton = { TextButton(onClick = { open = false }, enabled = !state.saving) { Text(stringResource(R.string.reports_cancel)) } },
    )
}

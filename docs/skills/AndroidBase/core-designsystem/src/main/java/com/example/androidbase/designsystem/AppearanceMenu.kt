package com.example.androidbase.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

@Composable
fun AppearanceMenu(selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val label = stringResource(if (selectedIndex == 2) R.string.base_theme_dark else R.string.base_theme_light)
    Column {
        IconButton(onClick = { expanded = true }, modifier = Modifier.semantics { stateDescription = label }) {
            Icon(Icons.Outlined.Contrast, stringResource(R.string.base_appearance))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(1, 2).forEach { index ->
                DropdownMenuItem(
                    text = { Text(stringResource(if (index == 2) R.string.base_theme_dark else R.string.base_theme_light)) },
                    onClick = { expanded = false; onSelect(index) },
                )
            }
        }
    }
}

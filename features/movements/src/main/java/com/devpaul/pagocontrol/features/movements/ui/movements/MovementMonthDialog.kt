package com.devpaul.pagocontrol.features.movements.ui.movements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.movements.R
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun MovementMonthDialog(selected: YearMonth, onDismiss: () -> Unit, onSelect: (YearMonth) -> Unit) {
    var year by rememberSaveable { mutableIntStateOf(selected.year) }
    var month by rememberSaveable { mutableIntStateOf(selected.monthValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_month)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { year-- }, enabled = year > 1) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.previous_year))
                    }
                    Text(year.toString(), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { year++ }, enabled = year < 9999) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, stringResource(R.string.next_year))
                    }
                }
                (1..12).chunked(3).forEach { months ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        months.forEach { number ->
                            FilterChip(
                                selected = month == number,
                                onClick = { month = number },
                                modifier = Modifier.weight(1f),
                                label = { Text(Month.of(number).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-PE"))) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(YearMonth.of(year, month)) }) {
                Text(stringResource(R.string.apply_month))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

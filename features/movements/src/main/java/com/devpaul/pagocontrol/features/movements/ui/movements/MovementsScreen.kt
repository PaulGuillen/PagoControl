package com.devpaul.pagocontrol.features.movements.ui.movements

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.InstitutionAppAvatar
import com.devpaul.pagocontrol.designsystem.InstitutionBadge
import com.devpaul.pagocontrol.designsystem.PagoBrand
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.designsystem.PagoDock
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.movements.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementsScreen(
    state: MovementsUiState,
    onIntent: (MovementsUiIntent) -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit = {},
    onReports: () -> Unit = {}
) {
    val context = LocalContext.current
    val clearedMessage = stringResource(R.string.filters_cleared)
    val resetFilters: () -> Unit = {
        onIntent(MovementsUiIntent.Reset)
        Toast.makeText(context, clearedMessage, Toast.LENGTH_SHORT).show()
    }
    val currentMonth = state.month == YearMonth.now()
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }
    val visible = state.visible
    val locale = Locale.forLanguageTag("es-PE")
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    val dayFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", locale)
    BackHandler(enabled = state.showAll) { onIntent(MovementsUiIntent.Back) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (state.showAll) TopAppBar(
                title = { Text(stringResource(R.string.all_movements)) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(MovementsUiIntent.Back) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = resetFilters) {
                        Icon(Icons.Outlined.FilterAltOff, stringResource(R.string.reset))
                    }
                },
            )
        },
        bottomBar = {
            if (!state.showAll) PagoDock(true, onHome, {}, onProfile = onProfile, onReports = onReports)
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            key(state.showAll) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!state.showAll) item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PagoBrand(
                                subtitle = stringResource(R.string.movements_title),
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = resetFilters) {
                                Icon(Icons.Outlined.FilterAltOff, stringResource(R.string.reset))
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = { onIntent(MovementsUiIntent.Search(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    stringResource(R.string.search_hint),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ManageSearch, null) },
                            trailingIcon = {
                                if (state.query.isNotEmpty()) IconButton(onClick = {
                                    onIntent(
                                        MovementsUiIntent.Search("")
                                    )
                                }) { Icon(Icons.Outlined.Close, stringResource(R.string.clear)) }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedBorderColor = Color.Transparent,
                            ),
                        )
                    }
                    item {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            listOf(
                                "all" to R.string.all,
                                "wallet" to R.string.wallet,
                                "gmail" to R.string.gmail
                            ).forEach { (key, label) ->
                                val channel = when (key) {
                                    "wallet" -> MovementChannel.Wallet
                                    "gmail" -> MovementChannel.Gmail
                                    else -> null
                                }
                                FilterChip(
                                    selected = state.channel == channel,
                                    onClick = { onIntent(MovementsUiIntent.Channel(channel)) },
                                    label = { Text(stringResource(label)) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = null,
                                )
                            }
                            FilledTonalButton(
                                onClick = { showMonthPicker = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (currentMonth) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (currentMonth) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                            ) {
                                Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(state.month.format(monthFormatter), maxLines = 1)
                            }
                        }
                    }
                    if (!state.showAll) {
                        MovementChannel.entries.filter { state.channel == null || state.channel == it }.forEach { channel ->
                            item {
                                MonthlyBalance(
                                    balance = state.balance(channel),
                                    channel = channel,
                                    month = state.month.format(monthFormatter),
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stringResource(R.string.recent_days), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                TextButton(onClick = { onIntent(MovementsUiIntent.ViewAll) }) {
                                    Text(stringResource(R.string.view_all))
                                }
                            }
                        }
                    }
                    if (state.loading) item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    if (state.error) item {
                        Column {
                            Text(stringResource(R.string.history_error), color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { onIntent(MovementsUiIntent.Retry) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                    if (!state.loading && !state.error && visible.isEmpty()) item {
                        Column(
                            Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, Modifier.size(40.dp))
                            Text(
                                stringResource(R.string.empty),
                                style = MaterialTheme.typography.titleSmall
                            )
                            TextButton(onClick = resetFilters) {
                                Text(
                                    stringResource(R.string.reset)
                                )
                            }
                        }
                    }
                    visible.groupBy { it.date }.forEach { (day, movements) ->
                        item(key = "day-$day") {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    day.format(dayFormatter),
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val net =
                                    movements.sumOf { it.amountCents }
                                Text(
                                    stringResource(R.string.net_amount, formatSoles(net, true)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (net >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        items(movements, key = { it.id }) { movement ->
                            HistoryRow(movement) {
                                onIntent(
                                    MovementsUiIntent.Review(movement)
                                )
                            }
                        }
                    }
                    if (state.hasMore) item {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onIntent(MovementsUiIntent.LoadMore) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Icon(Icons.Outlined.History, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.load_more))
                            }
                        }
                    }
                }
            }
        }
    }
    if (showMonthPicker) {
        MovementMonthDialog(
            selected = state.month,
            onDismiss = { showMonthPicker = false },
            onSelect = {
                onIntent(MovementsUiIntent.Month(it))
                showMonthPicker = false
            },
        )
    }
    state.reviewing?.let { movement ->
        AlertDialog(
            onDismissRequest = { onIntent(MovementsUiIntent.Dismiss) },
            title = { Text(movement.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${movement.detail}\n${formatSoles(movement.amountCents, true)}")
                    SelectionContainer {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(movement.notificationTitle, style = MaterialTheme.typography.titleMedium)
                            Text(movement.notificationText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onIntent(MovementsUiIntent.Dismiss) }) {
                    Text(
                        stringResource(R.string.close)
                    )
                }
            },
        )
    }
}

@Composable
private fun MonthlyBalance(balance: MonthlyMovementBalance, channel: MovementChannel, month: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Analytics, null, Modifier.size(18.dp))
                Text(
                    stringResource(R.string.balance_channel, stringResource(if (channel == MovementChannel.Wallet) R.string.wallet else R.string.gmail)),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    month,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                val total = balance.income - balance.expense
                if (total > 0) {
                    if (balance.income > 0) Box(
                        Modifier.weight(balance.income.toFloat() / total).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    if (balance.expense < 0) Box(
                        Modifier.weight(-balance.expense.toFloat() / total).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BalanceMetric(
                    stringResource(R.string.income),
                    balance.income,
                    MaterialTheme.colorScheme.secondary,
                    Modifier.weight(1f)
                )
                BalanceMetric(
                    stringResource(R.string.expense),
                    balance.expense,
                    MaterialTheme.colorScheme.tertiary,
                    Modifier.weight(1f)
                )
                BalanceMetric(
                    stringResource(R.string.net),
                    balance.net,
                    MaterialTheme.colorScheme.onSurface,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceMetric(label: String, amount: Long, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(formatSoles(amount, true), style = MaterialTheme.typography.titleSmall, color = color)
    }
}

@Composable
private fun HistoryRow(movement: HistoryMovement, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InstitutionAppAvatar(movement.source, movement.packageName)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        movement.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    InstitutionBadge(movement.source)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        movement.detail,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatSoles(movement.amountCents, true),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (movement.amountCents > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Preview(name = "Movimientos claro", showBackground = true)
@Composable
private fun MovementsLightPreview() {
    PagoControlTheme(false) { MovementsScreen(MovementsUiState(), {}, {}) }
}

@Preview(name = "Movimientos oscuro", showBackground = true)
@Composable
private fun MovementsDarkPreview() {
    PagoControlTheme(true) { MovementsScreen(MovementsUiState(), {}, {}) }
}

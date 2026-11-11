package com.devpaul.pagocontrol.features.movements.ui.movements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.*
import com.devpaul.pagocontrol.features.movements.R

@Composable
fun MovementsScreen(state: MovementsUiState, onIntent: (MovementsUiIntent) -> Unit, onHome: () -> Unit, onProfile: () -> Unit = {}, onReports: () -> Unit = {}) {
    val focus = remember { FocusRequester() }
    val context = LocalContext.current
    val exportLabel = stringResource(R.string.export)
    val visible = state.visible
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PagoDock(true, onHome, {}, onProfile = onProfile, onReports = onReports) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { PagoHeader(stringResource(R.string.movements_title), { onIntent(MovementsUiIntent.Upcoming) }) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.movements_title), style = MaterialTheme.typography.headlineMedium)
                                BadgeLabel(stringResource(R.string.demo), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Text(pluralStringResource(R.plurals.operation_count, visible.size, visible.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { focus.requestFocus() }) { Icon(Icons.Outlined.Search, stringResource(R.string.search)) }
                        IconButton(onClick = {
                            val report = visible.joinToString("\n") { "${it.title} • ${it.source} • ${formatSoles(it.amountCents, signed = it.kind != "transfer")}" }
                            context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "PagoControl · Movimientos de ejemplo\n$report")
                            }, exportLabel))
                        }) { Icon(Icons.Outlined.IosShare, stringResource(R.string.export)) }
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.query, onValueChange = { onIntent(MovementsUiIntent.Search(it)) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focus), singleLine = true,
                        placeholder = { Text(stringResource(R.string.search_hint), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ManageSearch, null) },
                        trailingIcon = { if (state.query.isNotEmpty()) IconButton(onClick = { onIntent(MovementsUiIntent.Search("")) }) { Icon(Icons.Outlined.Close, stringResource(R.string.clear)) } },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                    )
                }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("all" to R.string.all, "income" to R.string.income, "expense" to R.string.expense, "transfer" to R.string.transfers, "pending" to R.string.pending).forEach { (key, label) ->
                            val count = state.movements.count { key == "all" || it.kind == key || key == "pending" && it.pending }
                            FilterChip(
                                selected = state.filter == key, onClick = { onIntent(MovementsUiIntent.Filter(key)) },
                                label = { Text("${stringResource(label)}  $count") }, shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                border = null,
                            )
                        }
                    }
                }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterMenu(state.month, Icons.Outlined.CalendarMonth, listOf("Noviembre 2023", "Octubre 2023")) { onIntent(MovementsUiIntent.Month(it)) }
                        FilterMenu(stringResource(R.string.source, state.source), Icons.Outlined.AccountBalanceWallet, listOf("Todas", "Yape", "Plin", "BCP", "Interbank")) { onIntent(MovementsUiIntent.Source(it)) }
                        FilterMenu(stringResource(R.string.status, state.status), Icons.Outlined.VerifiedUser, listOf("Todos", "Confirmados", "Pendientes")) { onIntent(MovementsUiIntent.Status(it)) }
                        IconButton(onClick = { onIntent(MovementsUiIntent.Reset) }) { Icon(Icons.Outlined.FilterAltOff, stringResource(R.string.reset)) }
                    }
                }
                item { MonthlyBalance(state.month == "Noviembre 2023") }
                if (visible.isEmpty()) item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, Modifier.size(40.dp))
                        Text(stringResource(R.string.empty), style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { onIntent(MovementsUiIntent.Reset) }) { Text(stringResource(R.string.reset)) }
                    }
                }
                visible.groupBy { it.day }.forEach { (day, movements) ->
                    item(key = "day-$day") {
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.date_group, day), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val net = movements.filter { it.kind != "transfer" }.sumOf { it.amountCents }
                            Text(stringResource(R.string.net_amount, formatSoles(net, true)), style = MaterialTheme.typography.labelSmall, color = if (net >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    items(movements, key = { it.id }) { movement -> HistoryRow(movement) { onIntent(MovementsUiIntent.Review(movement)) } }
                }
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.shown, visible.size, state.movements.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilledTonalButton(onClick = { onIntent(MovementsUiIntent.LoadMore) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(16.dp)) {
                            Icon(Icons.Outlined.History, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.load_more))
                        }
                    }
                }
            }
        }
    }
    state.reviewing?.let { movement ->
        AlertDialog(
            onDismissRequest = { onIntent(MovementsUiIntent.Dismiss) },
            title = { Text(movement.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${movement.detail}\n${formatSoles(movement.amountCents, true)}")
                    if (movement.pending) {
                        Text(stringResource(R.string.choose_category))
                        listOf("Ventas", "Servicios", "Otros ingresos").forEach { category ->
                            TextButton(onClick = { onIntent(MovementsUiIntent.Categorize(category)) }) { Text(category) }
                        }
                    } else Text("${movement.badge} • ${movement.status}")
                }
            },
            confirmButton = { TextButton(onClick = { onIntent(MovementsUiIntent.Dismiss) }) { Text(stringResource(R.string.close)) } },
        )
    }
    state.notice?.let { notice ->
        AlertDialog(
            onDismissRequest = { onIntent(MovementsUiIntent.Dismiss) }, title = { Text(stringResource(R.string.demo_title)) },
            text = { Text(notice) },
            confirmButton = { TextButton(onClick = { onIntent(MovementsUiIntent.Dismiss) }) { Text(stringResource(R.string.close)) } },
        )
    }
}

@Composable
private fun FilterMenu(label: String, icon: ImageVector, choices: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(onClick = { expanded = true }, color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.heightIn(min = 48.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, Modifier.size(16.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
                Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { choice -> DropdownMenuItem(text = { Text(choice) }, onClick = { expanded = false; onSelect(choice) }) }
        }
    }
}

@Composable
private fun MonthlyBalance(hasData: Boolean) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Analytics, null, Modifier.size(18.dp))
                Text(stringResource(R.string.balance), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(if (hasData) R.string.month_range else R.string.empty_month_range), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                if (hasData) {
                    Box(Modifier.weight(0.751f).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
                    Box(Modifier.weight(0.249f).fillMaxHeight().background(MaterialTheme.colorScheme.tertiary))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BalanceMetric(stringResource(R.string.income), if (hasData) 685000 else 0, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                BalanceMetric(stringResource(R.string.expense), if (hasData) -226950 else 0, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                BalanceMetric(stringResource(R.string.net), if (hasData) 458050 else 0, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BalanceMetric(label: String, amount: Long, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatSoles(amount, true), style = MaterialTheme.typography.titleSmall, color = color)
    }
}

@Composable
private fun HistoryRow(movement: HistoryMovement, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tint = when {
        movement.pending -> colors.onPrimaryContainer
        movement.source == "Yape" -> if (EntryColors.isDark) Color(0xFFE58BF8) else Color(0xFF742284)
        movement.source == "Plin" -> Color(0xFF00A9E0)
        movement.kind == "transfer" -> colors.onSurfaceVariant
        movement.amountCents < 0 -> colors.tertiary
        else -> colors.secondary
    }
    val icon = when (movement.id) {
        "1" -> Icons.Outlined.PhoneAndroid
        "2" -> Icons.Outlined.LunchDining
        "3" -> Icons.Outlined.NotificationsActive
        "4" -> Icons.Outlined.Business
        "5" -> Icons.Outlined.SmartDisplay
        "6" -> Icons.Outlined.SwapHoriz
        "7" -> Icons.Outlined.QrCode2
        else -> Icons.Outlined.Bolt
    }
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = if (movement.pending) colors.primaryContainer.copy(alpha = 0.16f) else colors.surface,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(if (movement.pending) colors.primaryContainer else tint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(24.dp), tint = tint)
                }
                if (movement.kind != "transfer" && !movement.pending) {
                    Surface(modifier = Modifier.align(Alignment.BottomEnd).offset(4.dp, 4.dp), shape = CircleShape, color = if (movement.amountCents > 0) colors.secondary else colors.tertiary) {
                        Icon(if (movement.amountCents > 0) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward, null, Modifier.padding(2.dp).size(12.dp), tint = if (movement.amountCents > 0) colors.onSecondary else colors.onTertiary)
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movement.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(movement.detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                BadgeLabel(movement.badge, if (movement.pending) colors.tertiaryContainer else if (movement.id == "1") colors.secondaryContainer else tint.copy(alpha = 0.1f), if (movement.pending) colors.onTertiaryContainer else if (movement.id == "1") colors.onSecondaryContainer else tint)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(formatSoles(movement.amountCents, movement.kind != "transfer"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (movement.kind == "transfer" || movement.pending) colors.onSurface else if (movement.amountCents > 0) colors.secondary else colors.tertiary)
                if (movement.pending) BadgeLabel(stringResource(R.string.review), colors.primaryContainer, colors.onPrimaryContainer)
                else Text(movement.status, style = MaterialTheme.typography.labelSmall, color = if (movement.status == "Confirmado" || movement.status == "Pagado") colors.secondary else colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BadgeLabel(text: String, background: Color, foreground: Color) {
    Surface(color = background, contentColor = foreground, shape = RoundedCornerShape(5.dp)) {
        Text(text, Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(name = "Movimientos claro", showBackground = true)
@Composable
private fun MovementsLightPreview() { PagoControlTheme(false) { MovementsScreen(MovementsUiState(), {}, {}) } }

@Preview(name = "Movimientos oscuro", showBackground = true)
@Composable
private fun MovementsDarkPreview() { PagoControlTheme(true) { MovementsScreen(MovementsUiState(), {}, {}) } }

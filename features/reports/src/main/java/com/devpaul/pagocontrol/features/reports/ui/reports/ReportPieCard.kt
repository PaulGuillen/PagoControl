package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.reports.R
import kotlin.math.atan2
import kotlin.math.min

internal data class PieEntry(val name: String, val cents: Long, val color: Color? = null)

@Composable
internal fun ReportPieCard(title: String, entries: List<PieEntry>, income: Boolean? = null, onSelectMonth: (() -> Unit)? = null, onOpenAll: (() -> Unit)? = null, onSelect: (Int) -> Unit = {}) {
    val palette = reportChartPalette
    fun entryColor(index: Int) = entries[index].color ?: palette[index % palette.size]
    val total = entries.sumOf { it.cents.coerceAtLeast(0) }
    var selected by remember(entries) { mutableStateOf<Int?>(null) }
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
    }
    val emphasis = entries.indices.map { index ->
        animateFloatAsState(if (selected == index) 1f else 0f, tween(220), label = "segment emphasis").value
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val description = title + ": " + entries.joinToString { "${it.name} ${formatSoles(it.cents)}" }
    fun amount(cents: Long) = formatSoles(if (income == false) -cents else cents, signed = income != null)

    ReportCard {
        ReportSectionTitle(title, onSelectMonth)
        Text(stringResource(R.string.reports_chart_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(240.dp).semantics { contentDescription = description }
                .pointerInput(entries, total) {
                    detectTapGestures { position ->
                        if (total <= 0 || reveal.value < 0.99f) return@detectTapGestures
                        val delta = position - Offset(size.width / 2f, size.height / 2f)
                        val radius = min(size.width, size.height) / 2f - 12.dp.toPx()
                        val distance = delta.getDistance()
                        if (distance !in (radius - 18.dp.toPx())..(radius + 18.dp.toPx())) return@detectTapGestures
                        val angle = ((Math.toDegrees(atan2(delta.y.toDouble(), delta.x.toDouble())) + 450) % 360).toFloat()
                        var start = 0f
                        entries.forEachIndexed { index, entry ->
                            val sweep = (entry.cents.coerceAtLeast(0).toDouble() / total * 360).toFloat()
                            if (sweep > 0 && angle >= start && angle < start + sweep) {
                                selected = if (selected == index) null else index
                                return@detectTapGestures
                            }
                            start += sweep
                        }
                    }
                }) {
                val radius = size.minDimension / 2f - 12.dp.toPx()
                val bounds = Size(radius * 2, radius * 2)
                val origin = Offset(center.x - radius, center.y - radius)
                drawCircle(track, radius, style = Stroke(24.dp.toPx()))
                if (total > 0) {
                    var start = -90f
                    entries.forEachIndexed { index, entry ->
                        val sweep = (entry.cents.coerceAtLeast(0).toDouble() / total * 360).toFloat()
                        val gap = if (sweep < 359.9f) min(2.5f, sweep * 0.2f) else 0f
                        val visibleSweep = (min(sweep, reveal.value * 360 - (start + 90)) - gap).coerceAtLeast(0f)
                        if (visibleSweep > 0) drawArc(
                            entryColor(index).copy(alpha = if (selected == null) 1f else 0.35f + emphasis[index] * 0.65f),
                            start + gap / 2, visibleSweep, false, origin, bounds,
                            style = Stroke((24 + 8 * emphasis[index]).dp.toPx()),
                        )
                        start += sweep
                    }
                }
            }
            AnimatedContent(targetState = selected, label = "chart summary") { previousIndex ->
                val index = previousIndex?.takeIf { it in entries.indices }
                Column(Modifier.widthIn(max = 152.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(index?.let { entries[it].name } ?: stringResource(R.string.reports_chart_total), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(amount(index?.let { entries[it].cents } ?: total), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
                    if (index != null) Text(reportPercent(entries[index].cents.toDouble() / total.coerceAtLeast(1) * 100), color = entryColor(index), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        if (total == 0L) Text(stringResource(R.string.reports_chart_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            entries.forEachIndexed { index, entry ->
                val rowColor by animateColorAsState(if (selected == index) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface, label = "legend selection")
                Surface(color = rowColor, shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().selectable(selected == index, role = Role.RadioButton, onClick = { selected = if (selected == index) null else index }).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(10.dp).background(entryColor(index), CircleShape))
                        Text(entry.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(amount(entry.cents), style = MaterialTheme.typography.titleSmall)
                            Text(reportPercent(entry.cents.toDouble() / total.coerceAtLeast(1) * 100), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        if (onOpenAll != null) {
            TextButton(onClick = onOpenAll, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.reports_view_detail))
            }
        } else selected?.let { index ->
            TextButton(onClick = { onSelect(index) }, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.reports_view_detail))
            }
        }
    }
}

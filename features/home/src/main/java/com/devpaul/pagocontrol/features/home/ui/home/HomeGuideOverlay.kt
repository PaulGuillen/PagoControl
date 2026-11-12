package com.devpaul.pagocontrol.features.home.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.home.R
import kotlin.math.roundToInt

internal data class HomeGuideAction(
    val bounds: Rect,
    val label: String,
    val enabled: Boolean = true,
    val checked: Boolean? = null,
    val onClick: () -> Unit,
)

@Composable
internal fun HomeGuideOverlay(
    state: HomeUiState,
    target: Rect?,
    onIntent: (HomeUiIntent) -> Unit,
    permissionStep: Boolean = false,
    settingsFailed: Boolean = false,
    action: HomeGuideAction? = null,
) {
    val step = state.guideStep ?: return
    val titles = stringArrayResource(R.array.home_guide_titles)
    val descriptions = stringArrayResource(
        if (state.section == HomeSection.Mail) R.array.home_guide_mail else R.array.home_guide_wallets,
    )
    val progress = stringResource(R.string.home_guide_step, step + 1)
    val descriptionScroll = remember(step) { ScrollState(initial = 0) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    val localTarget = target?.translate(-origin)
    val highlight = MaterialTheme.colorScheme.primary
    BackHandler { onIntent(HomeUiIntent.DismissGuide) }
    Box(
        Modifier.fillMaxSize().testTag("home-guide-overlay")
            .onGloballyPositioned { origin = it.positionInRoot() }
            .pointerInput(Unit) { detectTapGestures {} }
            .semantics { paneTitle = progress },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val viewport = Rect(Offset.Zero, size)
            val visibleTarget = localTarget?.intersect(viewport)?.takeIf { it.width > 0 && it.height > 0 }
            val scrim = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(viewport)
                visibleTarget?.let { addRoundRect(RoundRect(it, CornerRadius(16.dp.toPx()))) }
            }
            drawPath(path = scrim, color = Color.Black.copy(alpha = 0.72f))
            visibleTarget?.let {
                drawRoundRect(
                    color = highlight,
                    topLeft = it.topLeft,
                    size = it.size,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
        }
        if (action != null) {
            Layout(
                modifier = Modifier.fillMaxSize(),
                content = {
                    val interaction = if (action.checked != null) {
                        Modifier.toggleable(value = action.checked, enabled = action.enabled, role = Role.Switch) { action.onClick() }
                    } else {
                        Modifier.clickable(enabled = action.enabled, role = Role.Button, onClick = action.onClick)
                    }
                    Box(
                        Modifier.testTag("home-guide-action").semantics { contentDescription = action.label }
                            .then(interaction),
                    )
                },
            ) { measurables, constraints ->
                val viewport = Rect(0f, 0f, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
                val bounds = action.bounds.translate(-origin).intersect(viewport)
                val control = measurables.single().measure(Constraints.fixed(
                    width = bounds.width.roundToInt().coerceAtLeast(0),
                    height = bounds.height.roundToInt().coerceAtLeast(0),
                ))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    control.place(x = bounds.left.roundToInt(), y = bounds.top.roundToInt())
                }
            }
        }
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Surface(
                    modifier = Modifier.testTag("home-guide-tooltip").semantics { liveRegion = LiveRegionMode.Polite },
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 12.dp,
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(progress, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (permissionStep) stringResource(R.string.notifications_access_title) else titles[step],
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Column(Modifier.weight(1f, fill = false).verticalScroll(descriptionScroll)) {
                            Text(
                                text = if (permissionStep) stringResource(R.string.notifications_access_guide) else descriptions[step],
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (permissionStep && settingsFailed) {
                                Text(stringResource(R.string.notifications_settings_error), color = MaterialTheme.colorScheme.error)
                            }
                        }
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = { onIntent(HomeUiIntent.DismissGuide) }) {
                                Text(stringResource(R.string.home_guide_cancel), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Button(onClick = { onIntent(HomeUiIntent.NextGuideStep) }) {
                                Text(stringResource(if (step == 3) R.string.home_guide_finish else R.string.home_guide_next))
                            }
                        }
                    }
                }
            },
        ) { measurables, constraints ->
            val margin = 16.dp.roundToPx()
            val gap = 12.dp.roundToPx()
            val viewport = Rect(0f, 0f, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
            val anchor = localTarget?.intersect(viewport) ?: Rect.Zero
            val above = (anchor.top.roundToInt() - gap - margin).coerceAtLeast(0)
            val below = (constraints.maxHeight - anchor.bottom.roundToInt() - gap - margin).coerceAtLeast(0)
            val placeBelow = below >= above
            val availableHeight = maxOf(above, below)
                .coerceAtLeast(minOf(240.dp.roundToPx(), constraints.maxHeight - margin * 2))
                .coerceAtMost(constraints.maxHeight - margin * 2)
                .coerceAtLeast(0)
            val width = (constraints.maxWidth - margin * 2).coerceIn(0, 520.dp.roundToPx())
            val tooltip = measurables.single().measure(Constraints.fixedWidth(width).copy(maxHeight = availableHeight))
            val desiredTop = if (placeBelow) anchor.bottom.roundToInt() + gap else anchor.top.roundToInt() - gap - tooltip.height
            val top = desiredTop.coerceIn(margin, maxOf(margin, constraints.maxHeight - margin - tooltip.height))
            layout(constraints.maxWidth, constraints.maxHeight) {
                tooltip.placeRelative(x = (constraints.maxWidth - tooltip.width) / 2, y = top)
            }
        }
    }
}

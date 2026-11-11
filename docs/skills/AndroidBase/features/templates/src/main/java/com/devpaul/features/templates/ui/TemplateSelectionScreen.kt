package com.devpaul.features.templates.ui

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.templates.R
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateDefinition
import com.devpaul.features.templates.ResumeTemplateRenderer

@Composable
fun TemplateSelectionScreen(
    onBack: () -> Unit,
    onContinue: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialTemplate: String = ResumeTemplateCatalog.BUDAPEST_ID,
    onDraftChange: (String) -> Unit = {},
) {
    var selectedId by rememberSaveable(initialTemplate) {
        mutableStateOf(ResumeTemplateCatalog.find(initialTemplate).id)
    }
    val currentOnDraftChange by rememberUpdatedState(onDraftChange)
    LaunchedEffect(selectedId) { currentOnDraftChange(selectedId) }
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        TemplateTopBar(onBack = onBack)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ui_choose_a_template_4d9458),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.ui_the_design_applies_to_both_the_preview_and_the_pdf_file_529017),
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(18.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = ResumeTemplateCatalog.templates,
                    key = ResumeTemplateDefinition::id,
                ) { template ->
                    TemplateOption(
                        template = template,
                        selected = template.id == selectedId,
                        onClick = trackedClick("select_template") {
                            AppAnalytics.selection("resume_template", template.id)
                            selectedId = template.id
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Button(
            onClick = trackedClick("continue") { onContinue(selectedId) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.ui_continue_to_preview_b47de1), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TemplateOption(
    template: ResumeTemplateDefinition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TemplateThumbnail(
                template = template,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = template.localizedName(),
                modifier = Modifier.heightIn(min = 20.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = template.localizedDescription(),
                modifier = Modifier.heightIn(min = 38.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.height(20.dp)) {
                if (selected) {
                    Text(
                        text = stringResource(R.string.ui_selected_ec2675),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateThumbnail(
    template: ResumeTemplateDefinition,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    shadowElevation: Dp = 0.dp,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD8DEE8)),
        shadowElevation = shadowElevation,
    ) {
        when (template.renderer) {
            ResumeTemplateRenderer.Budapest -> BudapestThumbnail()
            ResumeTemplateRenderer.Riga -> RigaThumbnail()
            ResumeTemplateRenderer.PragueEditorial -> PragueEditorialThumbnail()
            ResumeTemplateRenderer.TokyoCompact -> TokyoCompactThumbnail()
        }
    }
}

@Composable
private fun BudapestThumbnail() {
    Row {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.28f)
                .background(Color(0xFF414143)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.72f)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.fillMaxWidth(0.7f).height(12.dp).background(Color(0xFF414143)))
            Box(Modifier.fillMaxWidth(0.45f).height(5.dp).background(Color(0xFF9CA3AF)))
            Spacer(modifier = Modifier.height(12.dp))
            repeat(4) {
                Box(Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFD1D5DB)))
            }
        }
    }
}

@Composable
private fun RigaThumbnail() {
    val ink = Color(0xFF242424)
    val muted = Color(0xFFC4C7CC)
    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        RigaThumbnailVerticalLine(height = 18.dp, color = ink)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(0.72f)) {
                Box(Modifier.fillMaxWidth(0.82f).height(9.dp).background(ink))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth(0.52f).height(3.dp).background(Color(0xFF9CA3AF)))
            }
            Box(
                modifier = Modifier
                    .weight(0.28f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .width(28.dp)
                        .height(28.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(1.dp, ink),
                ) {}
            }
        }
        RigaThumbnailVerticalLine(height = 16.dp, color = ink)
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(5) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index % 2 == 0) 0.86f else 0.64f)
                            .height(if (index == 0) 5.dp else 3.dp)
                            .background(if (index == 0) ink else muted),
                    )
                }
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(ink))
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .padding(start = 9.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(ink))
                repeat(7) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index % 3 == 0) 0.7f else 1f)
                            .height(if (index % 3 == 0) 4.dp else 3.dp)
                            .background(if (index % 3 == 0) ink else muted),
                    )
                }
            }
        }
    }
}

@Composable
private fun RigaThumbnailVerticalLine(height: Dp, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        Box(Modifier.weight(0.3f).fillMaxHeight())
        Box(Modifier.width(1.dp).fillMaxHeight().background(color))
        Box(Modifier.weight(0.7f).fillMaxHeight())
    }
}

@Composable
private fun PragueEditorialThumbnail() {
    Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 15.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.43f)
                    .fillMaxHeight()
                    .padding(end = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(9.dp).background(Color(0xFF353535)))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF555555)))
                Box(Modifier.fillMaxWidth(0.68f).height(3.dp).background(Color(0xFF777777)))
                Spacer(Modifier.height(3.dp))
                repeat(2) {
                    Box(Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFFB8B8B8)))
                }
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF555555)))
            Column(
                modifier = Modifier
                    .weight(0.57f)
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.fillMaxWidth(0.45f).height(5.dp).background(Color(0xFF353535)))
                repeat(4) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index == 3) 0.72f else 1f)
                            .height(2.dp)
                            .background(Color(0xFFB8B8B8)),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF555555)))
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(0.29f)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                repeat(7) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index % 3 == 0) 1f else 0.78f)
                            .height(if (index % 3 == 0) 4.dp else 2.dp)
                            .background(if (index % 3 == 0) Color(0xFF353535) else Color(0xFFB8B8B8)),
                    )
                }
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF555555)))
            Column(
                modifier = Modifier
                    .weight(0.71f)
                    .fillMaxHeight()
                    .padding(start = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(10) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index % 4 == 0) 0.68f else 1f)
                            .height(if (index % 4 == 0) 4.dp else 2.dp)
                            .background(if (index % 4 == 0) Color(0xFF353535) else Color(0xFFB8B8B8)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TokyoCompactThumbnail() {
    val ink = Color(0xFF30333A)
    val muted = Color(0xFFB9BEC8)
    val section = Color(0xFFEAF2FF)
    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth(0.62f).height(11.dp).background(ink))
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth(0.36f).height(4.dp).background(Color(0xFF777B84)))
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth(0.74f).height(3.dp).background(muted))
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(2) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(section),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        repeat(4) { sectionIndex ->
            Box(Modifier.fillMaxWidth().height(12.dp).background(section))
            Spacer(Modifier.height(7.dp))
            repeat(if (sectionIndex == 1) 4 else 2) { lineIndex ->
                Box(
                    Modifier
                        .fillMaxWidth(if (lineIndex % 3 == 0) 1f else 0.82f)
                        .height(if (lineIndex == 0) 3.dp else 2.dp)
                        .background(if (lineIndex == 0) ink else muted),
                )
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}

@Composable
private fun TemplateTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = trackedClick("back", onBack)) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.ui_back_84a715),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = stringResource(R.string.ui_templates_0b4c44),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun TemplateSelectionPreview() {
    CvFreeTheme { TemplateSelectionScreen(onBack = {}, onContinue = {}) }
}

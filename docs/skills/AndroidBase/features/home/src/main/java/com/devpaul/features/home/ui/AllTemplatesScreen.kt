package com.devpaul.features.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.features.home.R
import com.devpaul.features.home.ui.util.localizedDescription
import com.devpaul.features.home.ui.util.localizedName
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateDefinition
import com.devpaul.features.templates.ResumeTemplateRenderer

private val GalleryBlue = Color(0xFF2563EB)

private enum class GalleryFilter(val label: Int) {
    All(R.string.gallery_filter_all),
    Executive(R.string.gallery_filter_executive),
    Minimal(R.string.gallery_filter_minimal),
    Tech(R.string.gallery_filter_tech),
    TwoColumns(R.string.gallery_filter_columns);

    fun includes(template: ResumeTemplateDefinition): Boolean = when (this) {
        All -> true
        Executive -> template.renderer in setOf(ResumeTemplateRenderer.Budapest, ResumeTemplateRenderer.PragueEditorial)
        Minimal -> template.renderer == ResumeTemplateRenderer.Riga
        Tech -> template.renderer == ResumeTemplateRenderer.TokyoCompact
        TwoColumns -> template.renderer != ResumeTemplateRenderer.TokyoCompact
    }
}

@Composable
fun AllTemplatesScreen(
    onBack: () -> Unit,
    onPreviewTemplate: (String) -> Unit,
    modifier: Modifier = Modifier,
    templates: List<ResumeTemplateDefinition> = ResumeTemplateCatalog.templates,
) {
    var filterName by rememberSaveable { mutableStateOf(GalleryFilter.All.name) }
    var alphabetical by rememberSaveable { mutableStateOf(false) }
    var selectedTemplateId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSort by remember { mutableStateOf(false) }
    val selectedFilter = GalleryFilter.valueOf(filterName)
    val namedTemplates = templates.map { it to it.localizedName() }
    val filteredTemplates = namedTemplates.filter { selectedFilter.includes(it.first) }
        .let { if (alphabetical) it.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { entry -> entry.second }) else it }
        .map { it.first }
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Column(modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding()) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = trackedClick("back", onBack),
                        modifier = Modifier.background(background, CircleShape),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.ui_back_84a715), Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.home_all_templates),
                        Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    ) {
                        Text(
                            pluralStringResource(R.plurals.gallery_active_templates, templates.size, templates.size),
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(GalleryFilter.entries, key = { it.name }) { filter ->
                        val active = filter == selectedFilter
                        Surface(
                            onClick = trackedClick("filter_templates") {
                                AppAnalytics.selection("template_filter", filter.name.lowercase())
                                filterName = filter.name
                            },
                            modifier = Modifier.semantics { selected = active },
                            shape = CircleShape,
                            color = if (active) GalleryBlue else MaterialTheme.colorScheme.surface,
                            border = if (active) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Text(
                                stringResource(filter.label),
                                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.gallery_subtitle),
                        Modifier.weight(1f).padding(start = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    )
                    Box {
                        TextButton(onClick = { showSort = true }) {
                            Text(stringResource(R.string.gallery_sort), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        DropdownMenu(expanded = showSort, onDismissRequest = { showSort = false }) {
                            listOf(false, true).forEach { byName ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(if (byName) R.string.gallery_sort_name else R.string.gallery_sort_default)) },
                                    modifier = Modifier.semantics { selected = alphabetical == byName },
                                    onClick = trackedClick("sort_templates") {
                                        alphabetical = byName
                                        showSort = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
            items(filteredTemplates, key = ResumeTemplateDefinition::id) { template ->
                AllTemplateCard(
                    template = template,
                    highlighted = selectedTemplateId == template.id,
                    onSelect = trackedClick("select_template") {
                        selectedTemplateId = template.id
                        AppAnalytics.selection("select_template", template.id)
                    },
                    onClick = trackedClick("preview_template") {
                        selectedTemplateId = template.id
                        AppAnalytics.selection("preview_template", template.id)
                        onPreviewTemplate(template.id)
                    },
                )
            }
            if (filteredTemplates.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(stringResource(R.string.gallery_empty), Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.gallery_tip), fontSize = 11.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AllTemplateCard(
    template: ResumeTemplateDefinition,
    highlighted: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.semantics { selected = highlighted },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (highlighted) 2.dp else 1.dp, if (highlighted) GalleryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        shadowElevation = if (highlighted) 3.dp else 1.dp,
    ) {
        Column(Modifier.padding(12.dp)) {
            CatalogTemplateThumbnail(template.renderer, Modifier.fillMaxWidth().aspectRatio(1f / 1.36f))
            Spacer(Modifier.height(10.dp))
            Text(
                template.localizedName(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                template.localizedDescription(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                border = if (highlighted) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (highlighted) GalleryBlue else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (highlighted) Color.White else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Outlined.Visibility, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.home_view), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(name = "All templates Light", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun AllTemplatesPreview() {
    CvFreeTheme(darkTheme = false) { AllTemplatesScreen(onBack = {}, onPreviewTemplate = {}) }
}

@Preview(name = "All templates Dark", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun AllTemplatesDarkPreview() {
    CvFreeTheme(darkTheme = true) { AllTemplatesScreen(onBack = {}, onPreviewTemplate = {}) }
}

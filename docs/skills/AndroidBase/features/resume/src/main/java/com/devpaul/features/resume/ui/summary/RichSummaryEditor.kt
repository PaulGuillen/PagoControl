package com.devpaul.features.resume.ui.summary

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.TrackAnalyticsView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeParagraphStyle
import com.devpaul.core.domain.resume.ResumeTextAlignment
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.ResumeTextSpan
import com.devpaul.features.resume.R

private const val DEFAULT_MAX_LENGTH = 1_500
private val EditorColors = listOf(0xFF1F2937L, 0xFFB91C1CL, 0xFF1D4ED8L, 0xFF047857L, 0xFF7E22CEL)
private val HighlightColors = listOf(0xFFFFF59DL, 0xFFFFCC80L, 0xFFA7F3D0L, 0xFFBFDBFEL, 0xFFE9D5FFL)

internal enum class InlineStyle { Bold, Italic, Underline, Strikethrough }

private data class CharacterStyle(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val textColorArgb: Long? = null,
    val backgroundColorArgb: Long? = null,
    val url: String? = null,
) {
    val isPlain: Boolean
        get() = !bold && !italic && !underline && !strikethrough &&
            textColorArgb == null && backgroundColorArgb == null && url == null

    fun asSpan(start: Int, end: Int) = ResumeTextSpan(
        start, end, bold, italic, underline, strikethrough,
        textColorArgb, backgroundColorArgb, url,
    )
}

@Stable
internal class RichTextEditorState(
    initialText: String,
    initialFormatting: ResumeTextFormatting,
    private val maxLength: Int = DEFAULT_MAX_LENGTH,
) {
    var value by mutableStateOf(TextFieldValue(initialText))
        private set
    var formatting by mutableStateOf(sanitizeFormatting(initialFormatting, initialText.length))
        private set
    private var typingStyle by mutableStateOf(CharacterStyle())

    val displayValue: TextFieldValue
        get() = value.copy(annotatedString = formattedSummary(value.text, formatting))

    fun onValueChange(newValue: TextFieldValue) {
        if (newValue.text.length > maxLength) return
        if (newValue.text != value.text) {
            formatting = remapFormatting(value.text, newValue.text, formatting, typingStyle)
        }
        value = newValue.copy(annotatedString = AnnotatedString(newValue.text))
    }

    fun toggle(style: InlineStyle) {
        val range = value.selection.normalized()
        if (range.collapsed) {
            typingStyle = typingStyle.toggle(style)
            return
        }
        updateSelectedCharacters { cells ->
            val remove = cells.all { it.enabled(style) }
            cells.map { it.with(style, !remove) }
        }
    }

    fun isActive(style: InlineStyle): Boolean {
        val range = value.selection.normalized()
        if (range.collapsed) return typingStyle.enabled(style)
        return formatting.characterStyles(value.text.length)
            .subList(range.start, range.end)
            .all { it.enabled(style) }
    }

    fun setTextColor(color: Long) = applyToSelectionOrTyping { copy(textColorArgb = color) }
    fun setHighlight(color: Long) = applyToSelectionOrTyping { copy(backgroundColorArgb = color) }

    fun setLink(url: String) {
        if (url.isNotBlank()) applyToSelectionOrTyping { copy(url = url.trim(), underline = true) }
    }

    fun cycleAlignment() {
        val range = paragraphRange(value.text, value.selection)
        val current = formatting.paragraphs.lastOrNull {
            it.start <= range.start && it.end >= range.end
        }?.alignment ?: ResumeTextAlignment.Start
        val next = when (current) {
            ResumeTextAlignment.Start -> ResumeTextAlignment.Center
            ResumeTextAlignment.Center -> ResumeTextAlignment.End
            ResumeTextAlignment.End -> ResumeTextAlignment.Justify
            ResumeTextAlignment.Justify -> ResumeTextAlignment.Start
        }
        formatting = formatting.copy(
            paragraphs = formatting.paragraphs
                .filterNot { it.start < range.end && it.end > range.start }
                .plus(ResumeParagraphStyle(range.start, range.end, next)),
        )
    }

    fun toggleList(numbered: Boolean) {
        val selected = paragraphRange(value.text, value.selection)
        val selectedText = value.text.substring(selected.start, selected.end)
        val numberPattern = Regex("^\\d+\\.\\s+")
        val bulletPattern = Regex("^•\\s+")
        val marker = if (numbered) "1. " else "• "

        if (selectedText.isBlank()) {
            val newText = value.text.replaceRange(selected.start, selected.end, marker)
            val cursor = selected.start + marker.length
            onValueChange(TextFieldValue(newText, TextRange(cursor)))
            return
        }

        val lines = selectedText.split('\n')
        val applied = lines.filter(String::isNotBlank).all {
            if (numbered) numberPattern.containsMatchIn(it) else bulletPattern.containsMatchIn(it)
        }
        val replacement = lines.mapIndexed { index, line ->
            val plain = line.replace(numberPattern, "").replace(bulletPattern, "")
            when {
                line.isBlank() -> line
                applied -> plain
                numbered -> "${index + 1}. $plain"
                else -> "• $plain"
            }
        }.joinToString("\n")
        val newText = value.text.replaceRange(selected.start, selected.end, replacement)
        val newSelection = if (value.selection.collapsed) {
            val previousPrefixLength = numberPattern.find(selectedText)?.value?.length
                ?: bulletPattern.find(selectedText)?.value?.length
                ?: 0
            val nextPrefixLength = if (applied) 0 else marker.length
            val cursor = (value.selection.start + nextPrefixLength - previousPrefixLength)
                .coerceIn(selected.start, selected.start + replacement.length)
            TextRange(cursor)
        } else {
            TextRange(selected.start, selected.start + replacement.length)
        }
        onValueChange(TextFieldValue(newText, newSelection))
    }

    private fun applyToSelectionOrTyping(transform: CharacterStyle.() -> CharacterStyle) {
        if (value.selection.collapsed) typingStyle = typingStyle.transform()
        else updateSelectedCharacters { cells -> cells.map(transform) }
    }

    private fun updateSelectedCharacters(transform: (List<CharacterStyle>) -> List<CharacterStyle>) {
        val range = value.selection.normalized()
        val cells = formatting.characterStyles(value.text.length)
        transform(cells.subList(range.start, range.end)).forEachIndexed { index, style ->
            cells[range.start + index] = style
        }
        formatting = formatting.copy(spans = cells.toSpans())
    }
}

@Composable
internal fun rememberRichTextEditorState(
    initialText: String,
    initialFormatting: ResumeTextFormatting,
    maxLength: Int = DEFAULT_MAX_LENGTH,
) = remember(initialText, initialFormatting, maxLength) {
    RichTextEditorState(initialText, initialFormatting, maxLength)
}

@Composable
internal fun RichTextEditor(
    state: RichTextEditorState,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 5,
) {
    var linkDialogVisible by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }

    Column(modifier) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FormattingToolbar(
            activeStyles = InlineStyle.entries.filter(state::isActive).toSet(),
            onBold = { state.toggle(InlineStyle.Bold) },
            onItalic = { state.toggle(InlineStyle.Italic) },
            onUnderline = { state.toggle(InlineStyle.Underline) },
            onStrikethrough = { state.toggle(InlineStyle.Strikethrough) },
            onNumberedList = { state.toggleList(true) },
            onBulletedList = { state.toggleList(false) },
            onLink = { linkDialogVisible = true },
            onTextColor = state::setTextColor,
            onHighlight = state::setHighlight,
            onAlignment = state::cycleAlignment,
        )
        OutlinedTextField(
            value = state.displayValue,
            onValueChange = state::onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                )
            },
            minLines = minLines,
            shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }

    if (linkDialogVisible) {
        TrackAnalyticsView("format_link_dialog")
        AlertDialog(
            onDismissRequest = { linkDialogVisible = false },
            title = { Text(stringResource(R.string.ui_add_link_44d638)) },
            text = {
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("URL") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = trackedClick("apply_link") {
                        state.setLink(linkUrl)
                        linkUrl = ""
                        linkDialogVisible = false
                    },
                    enabled = linkUrl.isNotBlank(),
                ) { Text(stringResource(R.string.ui_add_87ebee)) }
            },
            dismissButton = { TextButton(onClick = trackedClick("cancel_link") { linkDialogVisible = false }) { Text(stringResource(R.string.ui_cancel_c111e0)) } },
        )
    }
}

@Composable
private fun FormattingToolbar(
    activeStyles: Set<InlineStyle>,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrikethrough: () -> Unit,
    onNumberedList: () -> Unit,
    onBulletedList: () -> Unit,
    onLink: () -> Unit,
    onTextColor: (Long) -> Unit,
    onHighlight: (Long) -> Unit,
    onAlignment: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            itemVerticalAlignment = Alignment.CenterVertically,
            maxItemsInEachRow = 5,
        ) {
            ToolbarButton(Icons.Outlined.FormatBold, stringResource(R.string.ui_bold_9860e8), InlineStyle.Bold in activeStyles, onBold)
            ToolbarButton(Icons.Outlined.FormatItalic, stringResource(R.string.ui_italic_108bf6), InlineStyle.Italic in activeStyles, onItalic)
            ToolbarButton(Icons.Outlined.FormatUnderlined, stringResource(R.string.ui_underline_756180), InlineStyle.Underline in activeStyles, onUnderline)
            ToolbarButton(
                Icons.Outlined.FormatStrikethrough,
                stringResource(R.string.ui_strikethrough_5827c0),
                InlineStyle.Strikethrough in activeStyles,
                onStrikethrough,
            )
            ToolbarButton(Icons.Outlined.FormatListNumbered, stringResource(R.string.ui_numbered_list_621cc6), onClick = onNumberedList)
            ToolbarButton(
                Icons.AutoMirrored.Outlined.FormatListBulleted,
                stringResource(R.string.ui_bulleted_list_389a94),
                onClick = onBulletedList,
            )
            ToolbarButton(Icons.Outlined.Link, stringResource(R.string.ui_link_4b0eaf), onClick = onLink)
            ColorToolbarButton(Icons.Outlined.FormatColorText, stringResource(R.string.ui_text_color_6d0aeb), EditorColors, onTextColor)
            ColorToolbarButton(Icons.Outlined.FormatColorFill, stringResource(R.string.ui_highlight_cd5365), HighlightColors, onHighlight)
            ToolbarButton(Icons.AutoMirrored.Outlined.FormatAlignLeft, stringResource(R.string.ui_alignment_5d2d6a), onClick = onAlignment)
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    description: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = trackedClick("format_${icon.name.substringAfterLast('.').lowercase()}", onClick),
        modifier = Modifier
            .size(42.dp)
            .then(
                if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape)
                }
                else Modifier,
            ),
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun ColorToolbarButton(
    icon: ImageVector,
    description: String,
    colors: List<Long>,
    onColor: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolbarButton(icon, description, onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { color ->
                    IconButton(
                        onClick = trackedClick("select_format_color") { onColor(color); expanded = false },
                        modifier = Modifier.size(30.dp).background(Color(color), CircleShape),
                    ) {}
                }
            }
        }
    }
}

internal fun formattedSummary(text: String, formatting: ResumeTextFormatting): AnnotatedString =
    buildAnnotatedString {
        append(text)
        formatting.spans.forEach { span ->
            val start = span.start.coerceIn(0, text.length)
            val end = span.end.coerceIn(start, text.length)
            if (start == end) return@forEach
            addStyle(
                SpanStyle(
                    fontWeight = if (span.bold) FontWeight.Bold else null,
                    fontStyle = if (span.italic) FontStyle.Italic else null,
                    textDecoration = when {
                        span.underline && span.strikethrough -> TextDecoration.combine(
                            listOf(TextDecoration.Underline, TextDecoration.LineThrough),
                        )
                        span.underline -> TextDecoration.Underline
                        span.strikethrough -> TextDecoration.LineThrough
                        else -> null
                    },
                    color = span.textColorArgb?.let(::Color) ?: Color.Unspecified,
                    background = span.backgroundColorArgb?.let(::Color) ?: Color.Unspecified,
                ),
                start,
                end,
            )
        }
        formatting.paragraphs.forEach { paragraph ->
            val start = paragraph.start.coerceIn(0, text.length)
            val end = paragraph.end.coerceIn(start, text.length)
            if (start < end) addStyle(ParagraphStyle(textAlign = paragraph.alignment.toTextAlign()), start, end)
        }
    }

private fun ResumeTextAlignment.toTextAlign() = when (this) {
    ResumeTextAlignment.Start -> TextAlign.Start
    ResumeTextAlignment.Center -> TextAlign.Center
    ResumeTextAlignment.End -> TextAlign.End
    ResumeTextAlignment.Justify -> TextAlign.Justify
}

private fun sanitizeFormatting(value: ResumeTextFormatting, length: Int) = value.copy(
    spans = value.spans.mapNotNull { span ->
        val start = span.start.coerceIn(0, length)
        val end = span.end.coerceIn(start, length)
        span.copy(start = start, end = end).takeIf { start < end }
    },
    paragraphs = value.paragraphs.mapNotNull { paragraph ->
        val start = paragraph.start.coerceIn(0, length)
        val end = paragraph.end.coerceIn(start, length)
        paragraph.copy(start = start, end = end).takeIf { start < end }
    },
)

private fun ResumeTextFormatting.characterStyles(length: Int): MutableList<CharacterStyle> {
    val cells = MutableList(length) { CharacterStyle() }
    spans.forEach { span ->
        for (index in span.start.coerceAtLeast(0) until span.end.coerceAtMost(length)) {
            val current = cells[index]
            cells[index] = current.copy(
                bold = current.bold || span.bold,
                italic = current.italic || span.italic,
                underline = current.underline || span.underline,
                strikethrough = current.strikethrough || span.strikethrough,
                textColorArgb = span.textColorArgb ?: current.textColorArgb,
                backgroundColorArgb = span.backgroundColorArgb ?: current.backgroundColorArgb,
                url = span.url ?: current.url,
            )
        }
    }
    return cells
}

private fun List<CharacterStyle>.toSpans(): List<ResumeTextSpan> {
    if (isEmpty()) return emptyList()
    val spans = mutableListOf<ResumeTextSpan>()
    var start = 0
    for (index in 1..size) {
        if (index == size || this[index] != this[start]) {
            if (!this[start].isPlain) spans += this[start].asSpan(start, index)
            start = index
        }
    }
    return spans
}

private fun remapFormatting(
    oldText: String,
    newText: String,
    formatting: ResumeTextFormatting,
    typingStyle: CharacterStyle,
): ResumeTextFormatting {
    val prefix = oldText.commonPrefixWith(newText).length
    val suffix = oldText.substring(prefix).commonSuffixWith(newText.substring(prefix)).length
    val oldSuffixStart = oldText.length - suffix
    val newSuffixStart = newText.length - suffix
    val oldCells = formatting.characterStyles(oldText.length)
    val newCells = MutableList(newText.length) { typingStyle }
    for (index in 0 until prefix) newCells[index] = oldCells[index]
    for (offset in 0 until suffix) newCells[newSuffixStart + offset] = oldCells[oldSuffixStart + offset]

    val delta = newText.length - oldText.length
    val paragraphs = formatting.paragraphs.mapNotNull { paragraph ->
        val start = when {
            paragraph.start <= prefix -> paragraph.start
            paragraph.start >= oldSuffixStart -> paragraph.start + delta
            else -> prefix
        }.coerceIn(0, newText.length)
        val end = when {
            paragraph.end <= prefix -> paragraph.end
            paragraph.end >= oldSuffixStart -> paragraph.end + delta
            else -> newSuffixStart
        }.coerceIn(start, newText.length)
        paragraph.copy(start = start, end = end).takeIf { start < end }
    }
    return ResumeTextFormatting(newCells.toSpans(), paragraphs)
}

private fun CharacterStyle.enabled(style: InlineStyle) = when (style) {
    InlineStyle.Bold -> bold
    InlineStyle.Italic -> italic
    InlineStyle.Underline -> underline
    InlineStyle.Strikethrough -> strikethrough
}

private fun CharacterStyle.with(style: InlineStyle, enabled: Boolean) = when (style) {
    InlineStyle.Bold -> copy(bold = enabled)
    InlineStyle.Italic -> copy(italic = enabled)
    InlineStyle.Underline -> copy(underline = enabled)
    InlineStyle.Strikethrough -> copy(strikethrough = enabled)
}

private fun CharacterStyle.toggle(style: InlineStyle) = with(style, !enabled(style))
private fun TextRange.normalized() = TextRange(minOf(start, end), maxOf(start, end))

private fun paragraphRange(text: String, selection: TextRange): TextRange {
    val normalized = selection.normalized()
    val previousIndex = normalized.start - 1
    val start = if (previousIndex < 0) 0 else {
        text.lastIndexOf('\n', previousIndex).let { if (it == -1) 0 else it + 1 }
    }
    val end = text.indexOf('\n', normalized.end).let { if (it == -1) text.length else it }
    return TextRange(start, end)
}

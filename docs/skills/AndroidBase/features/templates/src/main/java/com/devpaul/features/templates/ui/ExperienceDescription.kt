package com.devpaul.features.templates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeTextFormatting

private val listPrefix = Regex("^\\s*((?:[•●▪◦·*]|-)|(?:\\d+[.)]))(?:\\s+|$)")

internal data class ExperienceDescriptionBlock(
    val marker: String?,
    val content: AnnotatedString,
)

internal fun experienceDescriptionBlocks(
    text: String,
    formatting: ResumeTextFormatting,
): List<ExperienceDescriptionBlock> {
    val formatted = formattedText(text, formatting)
    val blocks = mutableListOf<ExperienceDescriptionBlock>()
    var lineStart = 0

    while (lineStart <= formatted.length) {
        val newline = formatted.text.indexOf('\n', lineStart)
        val lineEnd = if (newline == -1) formatted.length else newline
        val line = formatted.text.substring(lineStart, lineEnd)
        if (line.isNotBlank()) {
            val prefix = listPrefix.find(line)
            val marker = prefix?.groupValues?.get(1)?.let { value ->
                if (value.firstOrNull()?.isDigit() == true) value else "•"
            }
            val contentStart = lineStart + (prefix?.range?.last?.plus(1) ?: 0)
            blocks += ExperienceDescriptionBlock(
                marker = marker,
                content = formatted.subSequence(contentStart, lineEnd),
            )
        }
        if (newline == -1) break
        lineStart = newline + 1
    }
    return blocks
}

@Composable
internal fun ExperienceDescription(
    text: String,
    formatting: ResumeTextFormatting,
    color: Color,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.5.dp,
) {
    val blocks = experienceDescriptionBlocks(text, formatting)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        blocks.forEach { block ->
            if (block.marker == null) {
                Text(
                    text = block.content,
                    color = color,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier.width(10.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        if (block.marker.firstOrNull()?.isDigit() == true) {
                            Text(
                                text = block.marker,
                                color = color,
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                textAlign = TextAlign.End,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(2.6.dp)
                                    .background(color, CircleShape),
                            )
                        }
                    }
                    Spacer(Modifier.width(3.5.dp))
                    Text(
                        text = block.content,
                        modifier = Modifier.weight(1f),
                        color = color,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                    )
                }
            }
        }
    }
}

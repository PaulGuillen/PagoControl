package com.devpaul.features.templates

import com.devpaul.core.domain.resume.ResumeParagraphStyle
import com.devpaul.core.domain.resume.ResumeTextFormatting

private val LeadingBullet = Regex("^[\\s\\u2022\\u00B7\\u25CF\\u25E6*-]+")

internal fun String.toAchievementBullets(): List<String> = lineSequence()
    .map { line -> line.replace(LeadingBullet, "").trim() }
    .filter(String::isNotEmpty)
    .toList()

internal data class StyledAchievementBullet(
    val marker: String,
    val text: String,
    val formatting: ResumeTextFormatting,
)

private val ListPrefix = Regex("^\\s*((\\d+)\\.|[\\u2022\\u00B7\\u25CF\\u25E6*-])\\s*")

internal fun String.toStyledAchievementBullets(
    formatting: ResumeTextFormatting,
): List<StyledAchievementBullet> {
    var lineOffset = 0
    return split('\n').mapNotNull { line ->
        val prefix = ListPrefix.find(line)
        val contentStart = prefix?.range?.last?.plus(1)
            ?: line.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
        val content = line.substring(contentStart).trimEnd()
        val globalStart = lineOffset + contentStart
        lineOffset += line.length + 1
        if (content.isBlank()) return@mapNotNull null
        val globalEnd = globalStart + content.length
        val localSpans = formatting.spans.mapNotNull { span ->
            val start = maxOf(span.start, globalStart)
            val end = minOf(span.end, globalEnd)
            span.copy(start = start - globalStart, end = end - globalStart).takeIf { start < end }
        }
        val alignment = formatting.paragraphs.lastOrNull { paragraph ->
            paragraph.start < globalEnd && paragraph.end > globalStart
        }?.alignment
        StyledAchievementBullet(
            marker = prefix?.groups?.get(2)?.value?.let { "$it." } ?: "•",
            text = content,
            formatting = ResumeTextFormatting(
                spans = localSpans,
                paragraphs = alignment?.let {
                    listOf(ResumeParagraphStyle(0, content.length, it))
                }.orEmpty(),
            ),
        )
    }
}

package com.devpaul.core.domain.resume

private val numberedListPrefix = Regex("^\\d+[.)]\\s+")
private val bulletCharacters = setOf('\u2022', '\u25CF', '\u25AA', '\u25E6', '\u00B7')

/**
 * Reconstructs paragraphs from line-wrapped text extracted from a PDF.
 *
 * PDF readers expose visual line wrapping as newline characters. Keeping every one of those
 * newlines makes the same text occupy only the original PDF column width in a new template.
 */
fun reflowSoftLineBreaks(text: String): String {
    if ('\n' !in text && '\r' !in text) return text

    val result = StringBuilder(text.length)
    var previousLine = ""
    var hasContent = false
    var hasBlankLine = false

    text.replace("\r\n", "\n")
        .replace('\r', '\n')
        .lineSequence()
        .forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) {
                if (hasContent) hasBlankLine = true
                return@forEach
            }

            if (hasContent) {
                val keepLineBreak = hasBlankLine || line.isListItem() || previousLine.endsParagraph()
                result.append(if (keepLineBreak) '\n' else ' ')
            }
            result.append(line)
            previousLine = line
            hasContent = true
            hasBlankLine = false
        }

    return result.toString()
}

private fun String.isListItem(): Boolean {
    val first = firstOrNull() ?: return false
    return first in bulletCharacters || startsWith("- ") || startsWith("* ") ||
        numberedListPrefix.containsMatchIn(this)
}

private fun String.endsParagraph(): Boolean {
    val lastMeaningful = trimEnd()
        .trimEnd('"', '\'', ')', ']', '}')
        .lastOrNull()
    return lastMeaningful in setOf('.', '!', '?', ':', ';')
}

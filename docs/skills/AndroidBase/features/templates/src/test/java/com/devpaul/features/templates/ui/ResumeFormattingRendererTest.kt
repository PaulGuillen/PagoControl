package com.devpaul.features.templates.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.devpaul.core.domain.resume.ResumeParagraphStyle
import com.devpaul.core.domain.resume.ResumeTextAlignment
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.ResumeTextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeFormattingRendererTest {

    @Test
    fun `renders persisted inline and paragraph formatting`() {
        val formatting = ResumeTextFormatting(
            spans = listOf(
                ResumeTextSpan(
                    start = 0,
                    end = 5,
                    bold = true,
                    italic = true,
                    underline = true,
                    strikethrough = true,
                    textColorArgb = 0xFF1D4ED8L,
                    backgroundColorArgb = 0xFFFFF59DL,
                ),
            ),
            paragraphs = listOf(
                ResumeParagraphStyle(0, 5, ResumeTextAlignment.Justify),
            ),
        )

        val rendered = formattedText("Texto", formatting)
        val span = rendered.spanStyles.single().item

        assertEquals(FontWeight.Bold, span.fontWeight)
        assertEquals(FontStyle.Italic, span.fontStyle)
        assertEquals(Color(0xFF1D4ED8L), span.color)
        assertEquals(Color(0xFFFFF59DL), span.background)
        assertTrue(span.textDecoration!!.contains(TextDecoration.Underline))
        assertTrue(span.textDecoration!!.contains(TextDecoration.LineThrough))
        assertEquals(TextAlign.Justify, rendered.paragraphStyles.single().item.textAlign)
    }

    @Test
    fun `separates bullets into readable experience blocks`() {
        val blocks = experienceDescriptionBlocks(
            text = "• Primera responsabilidad\n• Segunda responsabilidad",
            formatting = ResumeTextFormatting(),
        )

        assertEquals(2, blocks.size)
        assertEquals(listOf("•", "•"), blocks.map { it.marker })
        assertEquals(
            listOf("Primera responsabilidad", "Segunda responsabilidad"),
            blocks.map { it.content.text },
        )
    }

    @Test
    fun `supports numbered experience blocks`() {
        val blocks = experienceDescriptionBlocks(
            text = "1. Analizar requerimientos\n2. Implementar soluciones",
            formatting = ResumeTextFormatting(),
        )

        assertEquals(listOf("1.", "2."), blocks.map { it.marker })
        assertEquals(
            listOf("Analizar requerimientos", "Implementar soluciones"),
            blocks.map { it.content.text },
        )
    }
}

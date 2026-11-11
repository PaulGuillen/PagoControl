package com.devpaul.features.templates

import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.ResumeTextSpan
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementBulletsTest {
    @Test
    fun `removes user supplied bullet markers`() {
        val achievements = """
            • Primera responsabilidad
            · Segunda responsabilidad
            - Tercera responsabilidad
            * Cuarta responsabilidad
        """.trimIndent()

        assertEquals(
            listOf(
                "Primera responsabilidad",
                "Segunda responsabilidad",
                "Tercera responsabilidad",
                "Cuarta responsabilidad",
            ),
            achievements.toAchievementBullets(),
        )
    }

    @Test
    fun `ignores empty lines and standalone markers`() {
        assertEquals(
            listOf("Una responsabilidad", "Otra responsabilidad"),
            "•\n\nUna responsabilidad\n  • Otra responsabilidad  ".toAchievementBullets(),
        )
    }

    @Test
    fun `preserves numbered markers and remaps styles to their content`() {
        val achievements = "1. Diseñé la app\n2. Mejoré pruebas"
        val formatting = ResumeTextFormatting(
            spans = listOf(ResumeTextSpan(start = 3, end = 9, bold = true)),
        )

        val bullets = achievements.toStyledAchievementBullets(formatting)

        assertEquals(listOf("1.", "2."), bullets.map { it.marker })
        assertEquals(listOf("Diseñé la app", "Mejoré pruebas"), bullets.map { it.text })
        assertEquals(0, bullets.first().formatting.spans.single().start)
        assertEquals(6, bullets.first().formatting.spans.single().end)
    }
}

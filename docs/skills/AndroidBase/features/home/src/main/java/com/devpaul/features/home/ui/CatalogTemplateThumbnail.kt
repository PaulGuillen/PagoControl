package com.devpaul.features.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.devpaul.features.templates.ResumeTemplateRenderer

private val PreviewInk = Color(0xFF29303B)
private val PreviewMuted = Color(0xFF94A3B8)
private val PreviewLine = Color(0xFFCBD5E1)
private val PreviewBlue = Color(0xFF2563EB)

@Composable
internal fun CatalogTemplateThumbnail(renderer: ResumeTemplateRenderer, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            scale(scaleX = size.width / 100f, scaleY = size.height / 136f, pivot = Offset.Zero) {
                when (renderer) {
                    ResumeTemplateRenderer.Budapest -> {
                        drawRect(PreviewInk, size = Size(30f, 136f))
                        drawCircle(Color(0xFF556171), radius = 7f, center = Offset(15f, 14f))
                        catalogBar(4f, 27f, 22f, 3f, Color(0xFF596576))
                        catalogLines(4f, 33f, 21f, 3, Color(0xFF465263))
                        catalogLines(4f, 119f, 21f, 2, Color(0xFF465263))
                        catalogBar(36f, 8f, 49f, 7f, PreviewInk)
                        catalogBar(36f, 18f, 32f, 2.5f, PreviewMuted)
                        catalogLines(36f, 29f, 57f, 4)
                        catalogBar(36f, 56f, 39f, 4f, PreviewInk)
                        catalogLines(36f, 64f, 57f, 3)
                    }
                    ResumeTemplateRenderer.Riga -> {
                        catalogBar(7f, 8f, 61f, 5f, PreviewInk)
                        catalogBar(7f, 16f, 38f, 2f, PreviewMuted)
                        drawCircle(PreviewInk, 8f, Offset(85f, 15f), style = Stroke(0.7f))
                        drawCircle(Color(0xFFF1F5F9), 5.5f, Offset(85f, 15f))
                        catalogBar(7f, 27f, 86f, 0.6f, PreviewInk)
                        catalogBar(33f, 33f, 0.5f, 42f, PreviewLine)
                        catalogBar(7f, 34f, 23f, 3f, PreviewInk)
                        catalogLines(7f, 41f, 22f, 4, PreviewMuted)
                        catalogBar(38f, 34f, 43f, 4f, PreviewInk)
                        catalogLines(38f, 42f, 55f, 4)
                        catalogBar(38f, 65f, 48f, 4f, PreviewInk)
                        catalogLines(38f, 73f, 55f, 3)
                        catalogBar(7f, 122f, 86f, 0.5f, PreviewLine)
                        catalogBar(7f, 127f, 22f, 2f, PreviewLine)
                        catalogBar(75f, 127f, 18f, 2f, PreviewLine)
                    }
                    ResumeTemplateRenderer.PragueEditorial -> {
                        catalogBar(7f, 8f, 36f, 4f, PreviewInk)
                        catalogLines(7f, 15f, 36f, 2, PreviewMuted)
                        catalogBar(48f, 8f, 0.5f, 15f, PreviewLine)
                        catalogBar(54f, 8f, 31f, 4f, PreviewInk)
                        catalogLines(54f, 15f, 39f, 2, PreviewMuted)
                        catalogBar(7f, 29f, 86f, 0.6f, PreviewInk)
                        catalogBar(34f, 36f, 0.5f, 57f, PreviewLine)
                        repeat(2) { section ->
                            val sectionTop = 37f + section * 29f
                            catalogBar(7f, sectionTop, 23f, 3f, PreviewInk)
                            catalogLines(7f, sectionTop + 7f, 22f, 3)
                            catalogBar(40f, sectionTop, 37f, 3f, PreviewInk)
                            catalogLines(40f, sectionTop + 7f, 53f, 4)
                        }
                    }
                    ResumeTemplateRenderer.TokyoCompact -> {
                        catalogBar(20f, 8f, 60f, 7f, PreviewInk)
                        catalogBar(33f, 19f, 34f, 2.5f, PreviewLine)
                        catalogBar(7f, 28f, 41f, 4f, Color(0xFFEFF6FF))
                        catalogBar(52f, 28f, 41f, 4f, Color(0xFFEFF6FF))
                        repeat(3) { section ->
                            val sectionTop = 39f + section * 24f
                            catalogBar(7f, sectionTop, 86f, 3f, PreviewInk)
                            catalogBar(7f, sectionTop + 7f, 86f, 13f, Color(0xFFF8FAFC))
                            catalogBar(7f, sectionTop + 7f, 1.3f, 13f, PreviewBlue)
                            catalogLines(11f, sectionTop + 9f, 78f, 2)
                        }
                        drawCircle(Color(0xFF10B981), 1.5f, Offset(9f, 127f))
                        catalogBar(14f, 126f, 30f, 2f, PreviewMuted)
                    }
                }
            }
        }
    }
}

private fun DrawScope.catalogBar(left: Float, top: Float, width: Float, height: Float, color: Color) {
    drawRoundRect(color, Offset(left, top), Size(width, height), CornerRadius(0.4f))
}

private fun DrawScope.catalogLines(left: Float, top: Float, width: Float, count: Int, color: Color = PreviewLine) {
    repeat(count) { line ->
        catalogBar(left, top + line * 4.5f, width * if (line == count - 1) 0.78f else 1f, 1.6f, color)
    }
}

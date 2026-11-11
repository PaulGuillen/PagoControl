package com.devpaul.features.resume.importing

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.PDFTextStripperByArea
import java.text.Normalizer

internal data class ResumePdfTextContent(
    val fullText: String,
    val flowText: String = fullText,
    val headerText: String = "",
    val sidebarText: String = "",
    val mainText: String = fullText,
    val layoutText: String = "",
    val templateId: String = ResumeTemplateCatalog.BUDAPEST_ID,
)

internal enum class ResumePdfLayout(val templateId: String) {
    Budapest(ResumeTemplateCatalog.BUDAPEST_ID),
    Riga(ResumeTemplateCatalog.RIGA_ID),
    Prague(ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID),
    Tokyo(ResumeTemplateCatalog.TOKYO_COMPACT_ID),
    Generic(ResumeTemplateCatalog.BUDAPEST_ID),
}

internal object ResumePdfLayoutDetector {

    fun extract(document: PDDocument, declaredTemplate: String? = null): ResumePdfTextContent {
        val fullText = PDFTextStripper().apply { sortByPosition = true }.getText(document)
        val flowText = PDFTextStripper().apply { sortByPosition = false }.getText(document)
        val layout = declaredTemplate?.toLayout() ?: detect(document, fullText)
        val regions = extractRegions(document, layout)
        return ResumePdfTextContent(
            fullText = fullText,
            flowText = flowText,
            headerText = regions.header,
            sidebarText = regions.sidebar,
            mainText = regions.main.ifBlank { fullText },
            templateId = layout.templateId,
        )
    }

    private fun detect(document: PDDocument, text: String): ResumePdfLayout {
        val normalized = normalize(text)
        val page = runCatching {
            PDFRenderer(document).renderImageWithDPI(0, 48f, ImageType.RGB)
        }.getOrNull()
        try {
            if (page != null && page.darkSidebarRatio() >= 0.22f) return ResumePdfLayout.Budapest
            if (page != null && page.blueSectionRatio() >= 0.008f) return ResumePdfLayout.Tokyo
        } finally {
            page?.recycle()
        }

        return when {
            Regex("(?m)^CONTACTO$").containsMatchIn(normalized) -> ResumePdfLayout.Riga
            Regex("(?m)^EXPERIENCIA$").containsMatchIn(normalized) -> ResumePdfLayout.Prague
            Regex("(?m)^EXPERIENCIA LABORAL$").containsMatchIn(normalized) -> ResumePdfLayout.Budapest
            else -> ResumePdfLayout.Generic
        }
    }

    private data class ExtractedRegions(
        val header: String,
        val sidebar: String,
        val main: String,
    )

    private data class RegionProfile(
        val headerBottom: Float,
        val sidebarRight: Float,
        val contentTop: Float,
        val mainLeft: Float,
    )

    private fun extractRegions(document: PDDocument, layout: ResumePdfLayout): ExtractedRegions {
        val profile = when (layout) {
            ResumePdfLayout.Budapest -> RegionProfile(0.30f, 0.36f, 0f, 0.34f)
            ResumePdfLayout.Riga -> RegionProfile(0.19f, 0.34f, 0.12f, 0.26f)
            ResumePdfLayout.Prague -> RegionProfile(0.29f, 0.34f, 0.23f, 0.26f)
            ResumePdfLayout.Tokyo -> RegionProfile(0.20f, 0f, 0f, 0f)
            ResumePdfLayout.Generic -> RegionProfile(0.30f, 0f, 0f, 0f)
        }
        val header = StringBuilder()
        val sidebar = StringBuilder()
        val main = StringBuilder()

        document.pages.forEachIndexed { pageIndex, page ->
            val width = page.cropBox.width
            val height = page.cropBox.height
            if (pageIndex > 0) {
                header.appendLine()
                sidebar.appendLine()
                main.appendLine()
            }
            header.append(
                extractRegion(page, RectF(0f, 0f, width, height * profile.headerBottom)),
            )
            if (profile.sidebarRight > 0f) {
                sidebar.append(
                    extractRegion(
                        page,
                        RectF(0f, height * profile.contentTop, width * profile.sidebarRight, height),
                    ),
                )
            }
            main.append(
                extractRegion(
                    page,
                    RectF(width * profile.mainLeft, height * profile.contentTop, width, height),
                ),
            )
        }
        return ExtractedRegions(header.toString(), sidebar.toString(), main.toString())
    }

    private fun extractRegion(page: com.tom_roush.pdfbox.pdmodel.PDPage, area: RectF): String {
        val stripper = PDFTextStripperByArea().apply { sortByPosition = true }
        stripper.addRegion("content", area)
        stripper.extractRegions(page)
        return stripper.getTextForRegion("content")
    }

    private fun String.toLayout(): ResumePdfLayout? = when (this) {
        ResumeTemplateCatalog.BUDAPEST_ID -> ResumePdfLayout.Budapest
        ResumeTemplateCatalog.RIGA_ID -> ResumePdfLayout.Riga
        ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID -> ResumePdfLayout.Prague
        ResumeTemplateCatalog.TOKYO_COMPACT_ID -> ResumePdfLayout.Tokyo
        else -> null
    }

    private fun Bitmap.darkSidebarRatio(): Float {
        val right = (width * 0.34f).toInt().coerceAtLeast(1)
        var dark = 0
        var sampled = 0
        for (y in 0 until height step 3) {
            for (x in 0 until right step 3) {
                val color = getPixel(x, y)
                val red = Color.red(color)
                val green = Color.green(color)
                val blue = Color.blue(color)
                val brightness = (red + green + blue) / 3
                if (brightness < 170 && maxOf(red, green, blue) - minOf(red, green, blue) < 50) dark++
                sampled++
            }
        }
        return dark.toFloat() / sampled.coerceAtLeast(1)
    }

    private fun Bitmap.blueSectionRatio(): Float {
        var blue = 0
        var sampled = 0
        for (y in 0 until height step 3) {
            for (x in 0 until width step 3) {
                val color = getPixel(x, y)
                val red = Color.red(color)
                val green = Color.green(color)
                val blueValue = Color.blue(color)
                if (blueValue >= 220 && blueValue - red >= 10 && blueValue - green >= 5) blue++
                sampled++
            }
        }
        return blue.toFloat() / sampled.coerceAtLeast(1)
    }

    private fun normalize(value: String): String = Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase()
        .lineSequence()
        .joinToString("\n") { it.trim() }
}

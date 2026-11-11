package com.devpaul.features.resume.importing

import android.graphics.Rect
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal object ResumePdfOcr {

    private const val OCR_DPI = 250f

    suspend fun extract(document: PDDocument, templateId: String): ResumePdfTextContent {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val pages = mutableListOf<List<OcrLine>>()
        val renderer = PDFRenderer(document)
        try {
            repeat(document.numberOfPages) { pageIndex ->
                val bitmap = renderer.renderImageWithDPI(pageIndex, OCR_DPI, ImageType.RGB)
                try {
                    val result = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
                    pages += result.textBlocks
                        .flatMap { it.lines }
                        .mapNotNull { line ->
                            line.boundingBox?.let { bounds ->
                                OcrLine(
                                    text = line.text.trim(),
                                    bounds = bounds,
                                    pageWidth = bitmap.width,
                                    pageHeight = bitmap.height,
                                )
                            }
                        }
                        .filter { it.text.isNotBlank() }
                } finally {
                    bitmap.recycle()
                }
            }
        } finally {
            recognizer.close()
        }

        val profile = templateId.profile()
        val full = pages.joinToString("\n") { page -> page.inReadingOrder().joinToString("\n", transform = OcrLine::text) }
        val header = pages.firstOrNull().orEmpty()
            .filter { it.bounds.centerY() <= it.pageHeight * profile.headerBottom }
            .inReadingOrder()
            .joinToString("\n", transform = OcrLine::text)
        val sidebar = pages.joinToString("\n") { page ->
            page.filter { line ->
                profile.sidebarRight > 0f && line.bounds.centerX() <= line.pageWidth * profile.sidebarRight
            }.inReadingOrder().joinToString("\n", transform = OcrLine::text)
        }
        val main = pages.joinToString("\n") { page ->
            page.filter { line -> line.bounds.centerX() >= line.pageWidth * profile.mainLeft }
                .inReadingOrder()
                .joinToString("\n", transform = OcrLine::text)
        }
        val layout = pages.mapIndexed { pageIndex, page ->
            buildString {
                appendLine("PAGE ${pageIndex + 1}")
                page.inReadingOrder().forEach { line ->
                    val x = line.bounds.left * 100 / line.pageWidth
                    val y = line.bounds.top * 100 / line.pageHeight
                    appendLine("[x=$x y=$y] ${line.text}")
                }
            }.trimEnd()
        }.joinToString("\n")
        return ResumePdfTextContent(
            fullText = full,
            flowText = full,
            headerText = header,
            sidebarText = sidebar,
            mainText = main.ifBlank { full },
            layoutText = layout,
            templateId = templateId,
        )
    }

    private data class OcrProfile(
        val headerBottom: Float,
        val sidebarRight: Float,
        val mainLeft: Float,
    )

    private data class OcrLine(
        val text: String,
        val bounds: Rect,
        val pageWidth: Int,
        val pageHeight: Int,
    )

    private fun String.profile(): OcrProfile = when (this) {
        ResumeTemplateCatalog.BUDAPEST_ID -> OcrProfile(0.30f, 0.36f, 0.34f)
        ResumeTemplateCatalog.RIGA_ID -> OcrProfile(0.19f, 0.34f, 0.26f)
        ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID -> OcrProfile(0.29f, 0.34f, 0.26f)
        ResumeTemplateCatalog.TOKYO_COMPACT_ID -> OcrProfile(0.20f, 0f, 0f)
        else -> OcrProfile(0.30f, 0f, 0f)
    }

    private fun List<OcrLine>.inReadingOrder(): List<OcrLine> = sortedWith(
        compareBy<OcrLine> { it.bounds.top / 12 }
            .thenBy { it.bounds.left },
    )

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener { continuation.cancel() }
    }
}

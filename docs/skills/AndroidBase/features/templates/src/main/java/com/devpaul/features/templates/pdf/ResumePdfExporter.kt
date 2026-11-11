package com.devpaul.features.templates.pdf

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.devpaul.core.domain.resume.ResumeDocumentCodec
import com.devpaul.core.domain.resume.ResumeDocument
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer

object ResumePdfExporter {
    private const val PAGE_WIDTH = 595

    suspend fun export(
        context: Context,
        destination: Uri,
        preview: Bitmap,
        resume: ResumeDocument,
    ) = withContext(Dispatchers.IO) {
        require(preview.width > 0 && preview.height > 0) { "La vista previa está vacía" }
        val isHardwareBitmap = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            preview.config == Bitmap.Config.HARDWARE
        require(!isHardwareBitmap) {
            "La vista previa debe usar un bitmap compatible con Canvas"
        }

        PDFBoxResourceLoader.init(context.applicationContext)
        val pdf = PDDocument()
        try {
            pdf.documentInformation.apply {
                creator = "CVFree"
                producer = "CVFree"
                title = resume.displayName
                setCustomMetadataValue(ResumePdfMetadata.SCHEMA, ResumeDocumentCodec.SCHEMA_VERSION)
                setCustomMetadataValue(ResumePdfMetadata.TEMPLATE, resume.templateName)
                setCustomMetadataValue(ResumePdfMetadata.RESUME, ResumeDocumentCodec.encode(resume))
            }

            val scale = PAGE_WIDTH.toFloat() / preview.width
            val pageHeight = (preview.height * scale).coerceAtLeast(1f)
            val page = PDPage(PDRectangle(PAGE_WIDTH.toFloat(), pageHeight))
            pdf.addPage(page)
            val image = LosslessFactory.createFromImage(pdf, preview)
            PDPageContentStream(pdf, page).use { content ->
                content.drawImage(
                    image,
                    0f,
                    0f,
                    PAGE_WIDTH.toFloat(),
                    pageHeight,
                )
            }

            context.contentResolver.openOutputStream(destination)?.use(pdf::save)
                ?: error("No se pudo abrir el archivo seleccionado")
        } finally {
            pdf.close()
        }
    }

    fun suggestedFileName(resume: ResumeDocument): String {
        val rawName = listOf(resume.contact.name, resume.contact.lastName)
            .filter(String::isNotBlank)
            .joinToString("_")
            .ifBlank { "Mi_CV" }
        val asciiName = Normalizer.normalize(rawName, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace("[^A-Za-z0-9_-]".toRegex(), "_")
        return "CV_$asciiName.pdf"
    }
}

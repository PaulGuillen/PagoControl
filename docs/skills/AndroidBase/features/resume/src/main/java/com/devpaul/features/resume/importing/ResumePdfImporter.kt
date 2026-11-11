package com.devpaul.features.resume.importing

import com.devpaul.features.resume.R
import com.devpaul.core.platform.analytics.AppAnalytics

import android.content.Context
import android.net.Uri
import com.devpaul.core.domain.resume.ResumeDocumentCodec
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.features.templates.pdf.ResumePdfMetadata
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

internal object ResumePdfImporter {

    suspend fun import(
        context: Context,
        uri: Uri,
        policy: ResumeImportPolicy = ResumeImportPolicy(),
        onAiSkipped: (nextAvailableAtMillis: Long?) -> Unit = {},
        onAiEnhanced: () -> Unit = {},
        onProgress: suspend (progress: Int, stage: ResumeImportStage) -> Unit,
    ): ResumeDocument {
        onProgress(6, ResumeImportStage.Preparing)
        delay(250.milliseconds)

        onProgress(18, ResumeImportStage.Reading)
        val extracted = extractPdf(context, uri)
        extracted.embeddedDocument?.let { document ->
            onProgress(78, ResumeImportStage.Building)
            delay(300.milliseconds)
            onProgress(100, ResumeImportStage.Complete)
            delay(500.milliseconds)
            return document.copy(
                skills = ResumeTextParser.normalizeImportedSkills(document.skills),
            )
        }
        val content = extracted.textContent
        if (content == null || content.fullText.isBlank() && content.flowText.isBlank()) {
            throw ResumeImportException(
                R.string.ui_we_couldn_t_recognize_readable_text_try_a_clearer_or_higher_fa32fc,
            )
        }

        onProgress(48, ResumeImportStage.Extracting)
        delay(350.milliseconds)
        val document = withContext(Dispatchers.Default) {
            ResumeTextParser.parse(content)
        }

        val enhancedDocument = if (policy.aiEnhancementEnabled) {
            onProgress(66, ResumeImportStage.Enhancing)
            try {
                ResumeAiExtractor.enhance(context, content, document).also { enhanced ->
                    if (enhanced != null) onAiEnhanced() else onAiSkipped(null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ResumeAiQuotaException) {
                onAiSkipped(error.nextAvailableAtMillis)
                null
            } catch (error: Exception) {
                AppAnalytics.error("ai_resume_extraction", error)
                onAiSkipped(null)
                null
            }
        } else {
            null
        }

        onProgress(84, ResumeImportStage.Building)
        delay(450.milliseconds)
        onProgress(100, ResumeImportStage.Complete)
        delay(700.milliseconds)
        return enhancedDocument ?: document
    }

    private data class ExtractedPdf(
        val embeddedDocument: ResumeDocument? = null,
        val textContent: ResumePdfTextContent? = null,
    )

    private suspend fun extractPdf(context: Context, uri: Uri): ExtractedPdf = withContext(Dispatchers.IO) {
        PDFBoxResourceLoader.init(context.applicationContext)
        val input = context.contentResolver.openInputStream(uri)
            ?: throw ResumeImportException(R.string.ui_the_selected_file_couldn_t_be_opened_806cef)
        try {
            input.use { stream ->
                PDDocument.load(stream).use { document ->
                    if (document.isEncrypted) {
                        throw ResumeImportException(R.string.ui_the_pdf_is_password_protected_and_can_t_be_analyzed_efac06)
                    }
                    if (document.numberOfPages > 20) {
                        throw ResumeImportException(R.string.ui_the_document_exceeds_the_20_page_limit_ad18df)
                    }
                    val information = document.documentInformation
                    val embeddedDocument = information
                        .getCustomMetadataValue(ResumePdfMetadata.RESUME)
                        .let(ResumeDocumentCodec::decodeOrNull)
                    if (embeddedDocument != null) {
                        ExtractedPdf(embeddedDocument = embeddedDocument)
                    } else {
                        val declaredTemplate = information
                            .getCustomMetadataValue(ResumePdfMetadata.TEMPLATE)
                        val extractedText = ResumePdfLayoutDetector.extract(document, declaredTemplate)
                        ExtractedPdf(
                            textContent = if (extractedText.fullText.isNotBlank()) {
                                extractedText
                            } else {
                                try {
                                    ResumePdfOcr.extract(document, extractedText.templateId)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Exception) {
                                    throw ResumeImportException(
                                        R.string.ui_the_text_on_the_scanned_pages_couldn_t_be_recognized_ab09b4,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: ResumeImportException) {
            throw error
        } catch (_: Exception) {
            throw ResumeImportException(R.string.ui_the_file_isn_t_a_valid_pdf_or_is_damaged_8d3878)
        }
    }
}

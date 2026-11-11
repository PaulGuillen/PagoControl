package com.devpaul.core.data.resume

import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeTextFormatting
import com.devpaul.core.domain.resume.ResumeTextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreResumeCodecTest {
    @Test
    fun `cloud serialization preserves contact template and rich text formatting`() {
        val resume = ResumeDocument(
            id = "one",
            updatedAt = 123,
            templateName = "tokio",
            contact = ResumeContact(name = "Paul", professionalTitle = "Ingeniero"),
            summary = "Experiencia",
            summaryFormatting = ResumeTextFormatting(spans = listOf(ResumeTextSpan(0, 4, bold = true))),
        )
        assertEquals(resume, FirestoreResumeCodec.decode(resume.id, FirestoreResumeCodec.encode(resume)))
    }

    @Test
    fun `invalid identifiers and mismatched payloads are rejected`() {
        for (id in listOf("", " ", "../other", ".", "..", "a".repeat(201))) {
            assertTrue(runCatching { FirestoreResumeCodec.encode(ResumeDocument(id = id)) }.isFailure)
        }
        val data = FirestoreResumeCodec.encode(ResumeDocument(id = "one"))
        assertTrue(runCatching { FirestoreResumeCodec.decode("other", data) }.isFailure)
        assertTrue(runCatching { FirestoreResumeCodec.decode("one", null) }.isFailure)
        assertTrue(runCatching { FirestoreResumeCodec.decode("one", data + ("schemaVersion" to "2")) }.isFailure)
    }

    @Test
    fun `oversized UTF8 documents are rejected before enqueueing a write`() {
        val large = ResumeDocument(id = "one", summary = "界".repeat(260_000))
        assertTrue(runCatching { FirestoreResumeCodec.encode(large) }.isFailure)
    }

    @Test
    fun `migration identifiers are deterministic valid and separate from original identifiers`() {
        val migrated = legacyDocumentId("old")
        assertEquals(migrated, legacyDocumentId("old"))
        assertTrue(migrated != legacyDocumentId("another"))
        assertTrue(migrated.startsWith("legacy_"))
        FirestoreResumeCodec.requireValidId(migrated)
    }
}

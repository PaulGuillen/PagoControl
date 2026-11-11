package com.devpaul.core.data.resume

import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeDocumentCodec

internal object FirestoreResumeCodec {
    private const val MAX_CONTENT_BYTES = 750_000

    fun encode(resume: ResumeDocument): Map<String, Any> {
        requireValidId(resume.id)
        require(resume.updatedAt >= 0)
        val content = ResumeDocumentCodec.encode(resume)
        require(content.toByteArray(Charsets.UTF_8).size <= MAX_CONTENT_BYTES) { "Resume is too large" }
        return mapOf(
            "schemaVersion" to ResumeDocumentCodec.SCHEMA_VERSION,
            "content" to content,
            "updatedAt" to resume.updatedAt,
        )
    }

    fun decode(id: String, data: Map<String, Any>?): ResumeDocument {
        require(data?.get("schemaVersion") == ResumeDocumentCodec.SCHEMA_VERSION) { "Unsupported resume schema" }
        val resume = checkNotNull(ResumeDocumentCodec.decodeOrNull(data["content"] as? String)) {
            "Invalid resume content"
        }
        require(resume.id == id) { "Resume identifier mismatch" }
        return resume
    }

    fun requireValidId(id: String) {
        require(id.isNotBlank() && id.length <= 200 && '/' !in id && id != "." && id != "..") {
            "Invalid resume identifier"
        }
    }
}

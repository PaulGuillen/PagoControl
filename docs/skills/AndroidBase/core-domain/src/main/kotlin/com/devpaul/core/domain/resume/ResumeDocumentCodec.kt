package com.devpaul.core.domain.resume

import kotlinx.serialization.json.Json

object ResumeDocumentCodec {
    const val SCHEMA_VERSION = "1"
    private const val MAX_ENCODED_LENGTH = 1_000_000

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(document: ResumeDocument): String = json.encodeToString(document)

    fun decodeOrNull(value: String?): ResumeDocument? {
        if (value.isNullOrBlank() || value.length > MAX_ENCODED_LENGTH) return null
        return runCatching { json.decodeFromString<ResumeDocument>(value) }.getOrNull()
    }
}

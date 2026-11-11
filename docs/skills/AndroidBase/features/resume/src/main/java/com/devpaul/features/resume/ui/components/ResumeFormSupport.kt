package com.devpaul.features.resume.ui.components

import java.net.URI

internal const val NewEntryId = "__new_entry__"

internal fun String.isValidYearOrBlank(): Boolean =
    isBlank() || matches(Regex("\\d{4}"))

internal fun String.isValidMonthYearOrBlank(): Boolean =
    isBlank() || matches(Regex("\\d{4}")) || matches(Regex("(0[1-9]|1[0-2])/\\d{4}"))

internal fun String.isValidEmailOrBlank(): Boolean =
    isBlank() || matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))

internal fun String.isValidWebUrl(): Boolean = runCatching {
    val parsed = URI(trim())
    parsed.scheme in setOf("http", "https") && !parsed.host.isNullOrBlank()
}.getOrDefault(false)

internal inline fun <T> List<T>.replaceById(
    id: String,
    idOf: (T) -> String,
    replacement: T,
): List<T> = map { item -> if (idOf(item) == id) replacement else item }

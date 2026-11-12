package com.devpaul.pagocontrol.domain

object PaymentAmount {
    private val amountPattern = Regex("(?:S/\\.?|PEN)\\s*([0-9]+(?:[.,][0-9]+)*)", RegexOption.IGNORE_CASE)

    fun suggest(text: String): Long? {
        val matches = amountPattern.findAll(text).map { parse(it.groupValues[1]) }.toList()
        return matches.singleOrNull()
    }

    fun parse(value: String): Long? {
        val trimmed = value.trim()
        if (!trimmed.matches(Regex("[0-9]+(?:[.,][0-9]+)*"))) return null
        val normalized = when {
            trimmed.matches(Regex("[0-9]{1,3}(?:,[0-9]{3})+\\.[0-9]{2}")) -> trimmed.replace(",", "")
            trimmed.matches(Regex("[0-9]{1,3}(?:\\.[0-9]{3})+,[0-9]{2}")) -> trimmed.replace(".", "").replace(',', '.')
            trimmed.matches(Regex("[0-9]+[.,][0-9]{1,2}")) -> trimmed.replace(',', '.')
            trimmed.matches(Regex("[0-9]+")) -> trimmed
            else -> return null
        }
        return runCatching { normalized.toBigDecimal().movePointRight(2).longValueExact() }
            .getOrNull()?.takeIf { it in 1..999_999_999_99L }
    }
}

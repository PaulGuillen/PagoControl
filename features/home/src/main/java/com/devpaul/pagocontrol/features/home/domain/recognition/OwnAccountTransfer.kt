package com.devpaul.pagocontrol.features.home.domain.recognition

object OwnAccountTransfer {
    private val phrase =
        Regex("\\btransferencia\\s+entre\\s+mis\\s+cuentas\\b", RegexOption.IGNORE_CASE)

    fun matches(title: String, text: String): Boolean = phrase.containsMatchIn("$title\n$text")
}

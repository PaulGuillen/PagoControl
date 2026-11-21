package com.devpaul.pagocontrol.designsystem

import java.text.Normalizer
import java.util.Locale

/** Display labels only: recognition and stored financial data are unchanged. */
data class MovementLabels(val title: String, val institution: String, val fromGmail: Boolean)

private val bankNames = listOf(
    "BCP", "Interbank", "Scotiabank", "BBVA", "BanBif", "MiBanco", "Pichincha",
    "Compartamos", "GNB", "Alfin", "Agrobanco", "Banco de la Naci\u00f3n",
    "Banco de Comercio", "Banco Falabella", "Banco Ripley", "Banco Santander",
    "Caja Arequipa", "Caja Huancayo", "Caja Piura", "Caja Sullana", "Caja Cusco",
)
private val fieldNames = "destinatario|beneficiario|remitente|ordenante|comercio|establecimiento|empresa|destino|origen|monto(?: y moneda)?|importe|moneda|fecha(?: y hora)?|cuenta(?: cargo| destino| origen)?|c[o\u00f3]digo de operaci[o\u00f3]n|n[u\u00fa]mero de operaci[o\u00f3]n|concepto|descripci[o\u00f3]n|realiza mas operaciones|por tu seguridad"
private val fields = Regex(
    "(?:^|\\n)\\s*($fieldNames)\\s*:?\\s*\\n?\\s*(.+?)(?=\\n\\s*(?:$fieldNames)\\b|$)",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
)
private fun normalized(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
private fun compact(value: String) = value.replace(Regex("\\s+"), " ").trim()
private fun personName(value: String) = compact(value).split(' ').take(2).joinToString(" ")

fun movementLabels(
    title: String,
    source: String,
    packageName: String,
    notificationTitle: String,
    notificationText: String = "",
): MovementLabels {
    val fromGmail = packageName == "com.google.android.gm"
    val bank = sequenceOf(notificationTitle, source, title).mapNotNull { text ->
        bankNames.firstOrNull {
            Regex("\\b${Regex.escape(normalized(it))}\\b").containsMatchIn(normalized(text))
        }
    }.firstOrNull()
    val text = normalized("$notificationTitle\n$notificationText")
    val plin = Regex("\\bplin(?:eado|easte|eo|o|aste)?\\b").containsMatchIn(text)
    val yape = Regex("\\byape(?:o|aste|ado|ar)?\\b").containsMatchIn(text)
    val servicePayment = Regex("\\b(?:pago|pagar|pagaste|pagado|yapeo)\\s+(?:(?:de|del|un|el|tu)\\s+)*servicios?\\b|\\bdetalle del servicio\\b")
        .containsMatchIn(text)
    val institution = when {
        yape -> "Yape"
        fromGmail -> bank ?: source
        plin -> "Plin"
        else -> source
    }
    val values = fields.findAll(notificationText).map { match ->
        normalized(match.groupValues[1]) to compact(match.groupValues[2])
    }.toList()
    fun field(vararg names: String): String? = values.firstOrNull { it.first in names }
        ?.second?.takeIf { value ->
            value.length in 2..100 && value.any { it.isLetter() } &&
                !value.contains(Regex("\\d{6,}")) && !value.contains("S/", ignoreCase = true)
        }
    val merchant = field("comercio", "establecimiento", "empresa")
        ?: Regex(
            "\\b(?:consumo|compra|compraste|pagaste)\\b[^\\n]*?\\s+en\\s+(.+?)(?=\\s+(?:por tu seguridad|con tu tarjeta|por S/)|[\\n!?]|$)",
            RegexOption.IGNORE_CASE,
        ).find(notificationText)?.groupValues?.get(1)?.let { compact(it).removeSuffix(".") }
    val person = field("destinatario", "beneficiario", "remitente", "ordenante")
        ?: Regex("^(.+?)\\s+te\\s+(?:ha\\s+)?(?:envi[o\\u00f3]|plineado|pline[o\\u00f3])\\b", RegexOption.IGNORE_CASE)
            .find(notificationText.trim())?.groupValues?.get(1)
    val operation = when {
        servicePayment -> "Pago de servicio"
        plin -> "Pago Plin"
        yape -> "Pago Yape"
        Regex("\\b(reembolso|devolucion|extorno)\\b").containsMatchIn(text) -> "Devoluci\u00f3n"
        Regex("\\b(retiro|retiraste)\\b").containsMatchIn(text) -> "Retiro"
        Regex("\\b(compra|compraste|consumo)\\b").containsMatchIn(text) -> "Compra"
        Regex("\\b(transferencia|transferiste)\\b").containsMatchIn(text) -> "Transferencia"
        Regex("\\b(abono|deposito|depositaron)\\b").containsMatchIn(text) -> "Abono recibido"
        Regex("\\b(recibiste|recibido|recibida|te envio)\\b").containsMatchIn(text) -> "Pago recibido"
        Regex("\\b(pago|pagaste|pagado)\\b").containsMatchIn(text) -> "Pago"
        else -> null
    }
    val banklessTitle = if (bank == null) title else title.replaceFirst(
        Regex("^\\s*${Regex.escape(bank)}\\s*[-:\\u2013\\u2014]\\s*", RegexOption.IGNORE_CASE), "",
    )
    val strippedTitle = if (fromGmail) banklessTitle else banklessTitle.replaceFirst(
        Regex("^\\s*Plin\\s*[-:\\u00b7\\u2013\\u2014]\\s*", RegexOption.IGNORE_CASE), "",
    )
    val generic = normalized(title.trim()) in setOf("pago", "compra", "transferencia", "movimiento") ||
        title.equals(notificationTitle, ignoreCase = true) || bankNames.any {
        normalized(title).startsWith(normalized(it)) && strippedTitle == title
    } || Regex("servicio al|notificacion|constancia|confirmacion|alerta").containsMatchIn(normalized(title))
    val label = when {
        merchant != null -> merchant
        servicePayment -> "Pago de servicio"
        person != null -> if (plin && fromGmail) "Plin \u00b7 ${personName(person)}" else personName(person)
        !generic && strippedTitle.isNotBlank() -> if (fromGmail) strippedTitle else personName(strippedTitle)
        operation != null -> operation
        else -> if (fromGmail) strippedTitle else personName(strippedTitle)
    }
    return MovementLabels(compact(label).ifBlank { "Movimiento" }, institution, fromGmail)
}

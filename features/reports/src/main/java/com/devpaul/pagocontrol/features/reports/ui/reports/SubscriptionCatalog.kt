package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.domain.RecordedMovement
import com.devpaul.pagocontrol.domain.ReportSettings
import com.devpaul.pagocontrol.domain.SubscriptionRule
import java.text.Normalizer
import java.util.Locale

data class SubscriptionService(val name: String, val group: String, val aliases: List<String>)

object SubscriptionCatalog {
    val services = listOf(
        SubscriptionService("Netflix", "Video", listOf("netflix")),
        SubscriptionService("Disney+", "Video", listOf("disney+", "disney plus", "disneyplus")),
        SubscriptionService("HBO Max", "Video", listOf("hbo max", "hbomax", "max.com", "hbo digital")),
        SubscriptionService("Prime Video", "Video", listOf("prime video", "primevideo", "amazon prime", "amzn prime")),
        SubscriptionService("Crunchyroll", "Video", listOf("crunchyroll", "crunchy roll")),
        SubscriptionService("Paramount+", "Video", listOf("paramount+", "paramount plus", "paramountplus")),
        SubscriptionService("Apple TV+", "Video", listOf("apple tv+", "apple tv plus", "apple tv")),
        SubscriptionService("YouTube Premium", "Video", listOf("youtube premium", "google youtube", "google *youtube", "youtube membership")),
        SubscriptionService("ViX Premium", "Video", listOf("vix premium", "vix.com")),
        SubscriptionService("MUBI", "Video", listOf("mubi")),
        SubscriptionService("DGO", "Video", listOf("directv go", "directvgo", "dgo.tv")),
        SubscriptionService("Apple Music", "Música", listOf("apple music")),
        SubscriptionService("Spotify", "Música", listOf("spotify")),
        SubscriptionService("YouTube Music", "Música", listOf("youtube music")),
        SubscriptionService("Deezer", "Música", listOf("deezer")),
        SubscriptionService("Tidal", "Música", listOf("tidal")),
        SubscriptionService("Amazon Music", "Música", listOf("amazon music", "amzn music")),
        SubscriptionService("Google One", "Nube", listOf("google one", "google storage", "google *storage")),
        SubscriptionService("iCloud+", "Nube", listOf("icloud", "icloud+")),
        SubscriptionService("Dropbox", "Nube", listOf("dropbox")),
        SubscriptionService("Microsoft 365", "Productividad", listOf("microsoft 365", "office 365", "msbill.info")),
        SubscriptionService("Adobe Creative Cloud", "Productividad", listOf("adobe")),
        SubscriptionService("Canva", "Productividad", listOf("canva")),
        SubscriptionService("Notion", "Productividad", listOf("notion")),
        SubscriptionService("ChatGPT", "IA", listOf("chatgpt", "openai chatgpt", "openai *chatgpt")),
        SubscriptionService("Claude", "IA", listOf("claude.ai", "claude pro", "claude max")),
        SubscriptionService("GitHub Copilot", "IA", listOf("github copilot")),
        SubscriptionService("Perplexity", "IA", listOf("perplexity")),
        SubscriptionService("Xbox Game Pass", "Juegos", listOf("xbox game pass", "xbox gamepass", "microsoft game pass")),
        SubscriptionService("PlayStation Plus", "Juegos", listOf("playstation plus", "ps plus", "psn plus")),
        SubscriptionService("Nintendo Switch Online", "Juegos", listOf("nintendo switch online")),
        SubscriptionService("EA Play", "Juegos", listOf("ea play")),
        SubscriptionService("Ubisoft+", "Juegos", listOf("ubisoft+", "ubisoft plus")),
        SubscriptionService("Duolingo", "Educación", listOf("duolingo")),
        SubscriptionService("Coursera Plus", "Educación", listOf("coursera plus")),
        SubscriptionService("Platzi", "Educación", listOf("platzi")),
        SubscriptionService("LinkedIn Premium", "Productividad", listOf("linkedin premium")),
        SubscriptionService("Patreon", "Comunidades", listOf("patreon")),
        SubscriptionService("Twitch", "Comunidades", listOf("twitch")),
        SubscriptionService("Discord Nitro", "Comunidades", listOf("discord nitro")),
        SubscriptionService("Rappi Pro", "Membresías", listOf("rappi pro", "rappipro", "rappi prime")),
        SubscriptionService("Uber One", "Membresías", listOf("uber one", "uberone")),
    )

    fun detect(text: String, custom: List<SubscriptionRule> = emptyList()): String? {
        val normalized = normalizeReportText(text)
        fun matches(keyword: String): Boolean = Regex("(?<![a-z0-9])" + Regex.escape(normalizeReportText(keyword)) + "(?![a-z0-9])").containsMatchIn(normalized)
        return custom.firstOrNull { matches(it.keyword) }?.name
            ?: services.firstOrNull { service -> service.aliases.any(::matches) }?.name
    }
}

internal fun normalizeReportText(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace(Regex("\\s+"), " ").trim()

internal fun subscriptionList(report: FinancialReport, settings: ReportSettings): List<ReportSubscription> {
    val paid = report.subscriptions.associateBy { it.name }
    return (paid.keys + settings.rules.map { it.name })
        .distinct().filterNot { it in settings.hiddenSubscriptions }
        .map { paid[it] ?: ReportSubscription(it, 0L, 0) }
        .sortedWith(compareByDescending<ReportSubscription> { it.cents }.thenBy { it.name })
}

/** Only actual outgoing Gmail movements in the selected period can be added. */
internal fun availableEmailSubscriptions(movements: List<RecordedMovement>, report: FinancialReport, settings: ReportSettings): List<ReportExpense> {
    val gmail = movements.filter { it.packageName == "com.google.android.gm" && it.amountCents < 0 }.associateBy { it.id }
    val existing = subscriptionList(report, settings).map { normalizeReportText(it.name) }.toSet()
    return report.expenses.asSequence().filter { !it.manual && it.id in gmail }
        .map { expense ->
            val raw = gmail.getValue(expense.id)
            val name = SubscriptionCatalog.detect("${raw.title} ${raw.notificationTitle} ${raw.notificationText}", settings.rules) ?: expense.title
            expense.copy(title = name)
        }
        .filter { normalizeReportText(it.title) !in existing }
        .distinctBy { normalizeReportText(it.title) }.toList()
}

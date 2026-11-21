package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.designsystem.movementLabels
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.RecordedMovement
import com.devpaul.pagocontrol.domain.ReportSettings
import com.devpaul.pagocontrol.features.reports.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class ReportPeriod { Month, Quarter, Year, Custom }
enum class ReportFormat(val extension: String, val mimeType: String) {
    Pdf("pdf", "application/pdf"),
    Excel("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    Csv("csv", "text/csv"),
}
fun ExpenseCategory.label(): Int = when (this) {
    ExpenseCategory.Food -> R.string.reports_category_food
    ExpenseCategory.Services -> R.string.reports_category_services
    ExpenseCategory.Shopping -> R.string.reports_category_shopping
    ExpenseCategory.Subscriptions -> R.string.reports_category_subscriptions
    ExpenseCategory.Transport -> R.string.reports_category_transport
    ExpenseCategory.Games -> R.string.reports_category_games
    else -> R.string.reports_category_other
}
data class ReportAmount(val label: Int, val cents: Long, val category: ExpenseCategory? = null)
data class ReportChannel(val source: String, val cents: Long, val operations: Int, val income: Long = 0, val expense: Long = 0)
data class ReportIncome(val id: String, val title: String, val cents: Long, val date: LocalDate, val channel: String)
data class ReportPoint(val number: Int, val income: Long, val expense: Long)
data class ReportSubscription(val name: String, val cents: Long, val payments: Int)
data class ReportExpense(val id: String, val title: String, val cents: Long, val date: LocalDate, val channel: String, val category: ExpenseCategory, val manual: Boolean, val subscription: String?)
data class FinancialReport(
    val start: LocalDate,
    val end: LocalDate,
    val income: Long,
    val expense: Long,
    val incomeCount: Int,
    val expenseCount: Int,
    val points: List<ReportPoint>,
    val categories: List<ReportAmount>,
    val channels: List<ReportChannel>,
    val subscriptions: List<ReportSubscription>,
    val expenses: List<ReportExpense>,
    val incomes: List<ReportIncome> = emptyList(),
) {
    val firstMonth: Int get() = start.monthValue
    val lastMonth: Int get() = end.monthValue
    val net: Long get() = income - expense
    val weekly: Boolean get() = start.year == end.year && firstMonth == lastMonth
}
data class ReportExport(val report: FinancialReport, val format: ReportFormat)

private fun reportChannel(value: String): String = when (normalizeReportText(value)) {
    "gmail" -> "Sin identificar"
    "bcp" -> "BCP"
    "bbva" -> "BBVA"
    "yape" -> "Yape"
    "plin" -> "Plin"
    "interbank" -> "Interbank"
    "efectivo" -> "Efectivo"
    else -> value.trim()
}

internal fun buildReport(movements: List<RecordedMovement>, settings: ReportSettings, start: LocalDate, end: LocalDate, zone: ZoneId = ZoneId.systemDefault()): FinancialReport {
    require(!start.isAfter(end))
    fun inRange(date: LocalDate) = !date.isBefore(start) && !date.isAfter(end)
    val recorded = movements.distinctBy { it.id }.filter { inRange(Instant.ofEpochMilli(it.postedAt).atZone(zone).toLocalDate()) }
    val expenses = recorded.filter { it.amountCents < 0 }.map { item ->
        val labels = movementLabels(item.title, item.source, item.packageName, item.notificationTitle, item.notificationText)
        val subscription = if (item.packageName == "com.google.android.gm") SubscriptionCatalog.detect("${item.title} ${item.notificationTitle} ${item.notificationText}", settings.rules) else null
        val category = settings.categories[item.id] ?: when {
            subscription != null -> ExpenseCategory.Subscriptions
            labels.title == "Pago de servicio" -> ExpenseCategory.Services
            else -> ExpenseCategory.Other
        }
        ReportExpense(item.id, labels.title, abs(item.amountCents), Instant.ofEpochMilli(item.postedAt).atZone(zone).toLocalDate(), reportChannel(labels.institution), category, false, if (category == ExpenseCategory.Subscriptions) subscription ?: labels.title else null)
    } + settings.expenses.filter { inRange(LocalDate.parse(it.date)) }.map {
        ReportExpense(it.id, it.title, it.cents, LocalDate.parse(it.date), reportChannel(it.channel), it.category, true,
            if (it.category == ExpenseCategory.Subscriptions) SubscriptionCatalog.detect(it.title, settings.rules) ?: it.title else null)
    }
    val incomeItems = recorded.filter { it.amountCents > 0 }
    val income = incomeItems.sumOf { it.amountCents }
    val expense = expenses.sumOf { it.cents }
    val incomes = incomeItems.map { item ->
        val labels = movementLabels(item.title, item.source, item.packageName, item.notificationTitle, item.notificationText)
        ReportIncome(item.id, labels.title, item.amountCents, Instant.ofEpochMilli(item.postedAt).atZone(zone).toLocalDate(), reportChannel(labels.institution))
    }.sortedByDescending { it.date }
    val channelNames = (incomes.map { it.channel } + expenses.map { it.channel }).distinct()
    val channels = channelNames.map { name ->
        val incoming = incomes.filter { it.channel == name }
        val outgoing = expenses.filter { it.channel == name }
        val incomingTotal = incoming.sumOf { it.cents }
        val outgoingTotal = outgoing.sumOf { it.cents }
        ReportChannel(name, incomingTotal + outgoingTotal, incoming.size + outgoing.size, incomingTotal, outgoingTotal)
    }.sortedByDescending { it.cents }
    val weekly = start.year == end.year && start.monthValue == end.monthValue
    val pointCount = if (weekly) (end.dayOfMonth + 6) / 7 else ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)).toInt() + 1
    fun point(date: LocalDate) = if (weekly) (date.dayOfMonth - 1) / 7 else ChronoUnit.MONTHS.between(start.withDayOfMonth(1), date.withDayOfMonth(1)).toInt()
    val points = (0 until pointCount).map { index -> ReportPoint(if (weekly) index + 1 else start.plusMonths(index.toLong()).monthValue,
        incomeItems.filter { point(Instant.ofEpochMilli(it.postedAt).atZone(zone).toLocalDate()) == index }.sumOf { it.amountCents },
        expenses.filter { point(it.date) == index }.sumOf { it.cents }) }
    return FinancialReport(start, end, income, expense, incomeItems.size, expenses.size, points,
        (ExpenseCategory.entries + settings.customCategories + expenses.map { it.category }).distinct().map { category -> ReportAmount(category.label(), expenses.filter { it.category == category }.sumOf { it.cents }, category) },
        channels, expenses.filter { it.subscription != null }.groupBy { it.subscription!! }.map { (name, items) -> ReportSubscription(name, items.sumOf { it.cents }, items.size) }.sortedByDescending { it.cents },
        expenses.sortedByDescending { it.date }, incomes)
}

internal fun FinancialReport.matchesPoint(date: LocalDate, index: Int?): Boolean = index == null ||
    if (weekly) (date.dayOfMonth - 1) / 7 == index else ChronoUnit.MONTHS.between(start.withDayOfMonth(1), date.withDayOfMonth(1)).toInt() == index

package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.features.reports.R

enum class ReportPeriod { Month, Quarter, Year, Custom }
internal val demoSubscriptions = listOf("Netflix" to 4490L, "Spotify" to 2090L, "Google One" to 650L, "Entel" to 7990L, "Claro Hogar" to 13330L)
enum class ReportFormat(val extension: String, val mimeType: String) {
    Pdf("pdf", "application/pdf"),
    Excel("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    Csv("csv", "text/csv"),
}

data class ReportAmount(val label: Int, val cents: Long)
data class ReportChannel(val source: String, val label: Int, val cents: Long, val operations: Int)
data class ReportPoint(val number: Int, val income: Long, val expense: Long)

data class FinancialReport(
    val firstMonth: Int,
    val lastMonth: Int,
    val income: Long,
    val expense: Long,
    val incomeCount: Int,
    val expenseCount: Int,
    val previousNet: Long?,
    val points: List<ReportPoint>,
    val categories: List<ReportAmount>,
    val channels: List<ReportChannel>,
) {
    val net: Long get() = income - expense
    val savingsRate: Double get() = if (income == 0L) 0.0 else net * 100.0 / income
    val change: Double? get() = previousNet?.takeIf { it != 0L }?.let { (net - it) * 100.0 / kotlin.math.abs(it) }
    val weekly: Boolean get() = firstMonth == lastMonth
}

data class ReportExport(val report: FinancialReport, val format: ReportFormat)

internal fun distributeCents(total: Long, weights: List<Long>): List<Long> {
    val sum = weights.sum()
    val allocated = weights.dropLast(1).map { total * it / sum }
    return allocated + (total - allocated.sum())
}

internal fun demoReport(firstMonth: Int = 11, lastMonth: Int = 11): FinancialReport {
    require(firstMonth in 1..11 && lastMonth in firstMonth..11)
    val incomes = listOf(520000L, 545000L, 560000L, 585000L, 600000L, 565000L, 620000L, 580000L, 590000L, 610000L, 685000L)
    val expenses = listOf(240000L, 235000L, 241000L, 252000L, 248000L, 239000L, 260000L, 245000L, 239000L, 222500L, 226950L)
    val months = firstMonth..lastMonth
    val income = months.sumOf { incomes[it - 1] }
    val expense = months.sumOf { expenses[it - 1] }
    val count = months.count()
    val subscription = demoSubscriptions.sumOf { it.second } * count
    val otherCategories = distributeCents(expense - subscription, listOf(78000L, 45000L, 41000L, 21000L, 13400L))
    val categories = listOf(
        ReportAmount(R.string.reports_category_food, otherCategories[0]),
        ReportAmount(R.string.reports_category_services, otherCategories[1]),
        ReportAmount(R.string.reports_category_shopping, otherCategories[2]),
        ReportAmount(R.string.reports_category_subscriptions, subscription),
        ReportAmount(R.string.reports_category_transport, otherCategories[3]),
        ReportAmount(R.string.reports_category_other, otherCategories[4]),
    )
    val incomeCount = months.sumOf { (18L * incomes[it - 1] / 685000).toInt() }
    val expenseCount = months.sumOf { (34L * expenses[it - 1] / 226950).toInt() }
    val channelAmounts = distributeCents(income + expense, listOf(342000L, 225000L, 189000L, 155950L))
    val channelCounts = distributeCents((incomeCount + expenseCount).toLong(), listOf(24L, 12L, 10L, 6L))
    val labels = listOf(R.string.reports_channel_yape, R.string.reports_channel_bcp, R.string.reports_channel_plin, R.string.reports_channel_interbank)
    val sources = listOf("Yape", "BCP", "Plin", "Interbank")
    val points = if (count == 1) {
        val weeklyIncome = distributeCents(income, listOf(175000L, 150000L, 215000L, 145000L))
        val weeklyExpense = distributeCents(expense, listOf(65000L, 49950L, 62000L, 50000L))
        (1..4).map { ReportPoint(it, weeklyIncome[it - 1], weeklyExpense[it - 1]) }
    } else months.map { ReportPoint(it, incomes[it - 1], expenses[it - 1]) }
    val previousStart = firstMonth - count
    val previousNet = if (previousStart >= 1) (previousStart until firstMonth).sumOf { incomes[it - 1] - expenses[it - 1] } else null
    return FinancialReport(
        firstMonth, lastMonth, income, expense, incomeCount, expenseCount, previousNet, points, categories,
        sources.mapIndexed { index, source -> ReportChannel(source, labels[index], channelAmounts[index], channelCounts[index].toInt()) },
    )
}

package com.devpaul.pagocontrol.features.reports.ui.reports

import java.time.LocalDate

internal data class ReportFlowMovement(
    val key: String,
    val title: String,
    val cents: Long,
    val date: LocalDate,
    val channel: String,
)

internal fun FinancialReport.flowMovements(query: String = ""): Map<LocalDate, List<ReportFlowMovement>> {
    val search = normalizeReportText(query)
    return (incomes.map { ReportFlowMovement("income-${it.id}", it.title, it.cents, it.date, it.channel) } +
        expenses.map { ReportFlowMovement("expense-${it.manual}-${it.id}", it.title, -it.cents, it.date, it.channel) })
        .filter { normalizeReportText("${it.title} ${it.channel}").contains(search) }
        .sortedByDescending { it.date }
        .groupBy { it.date }
}

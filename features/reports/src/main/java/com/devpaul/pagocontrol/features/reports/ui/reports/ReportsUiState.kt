package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.domain.RecordedMovement
import com.devpaul.pagocontrol.domain.ReportSettings
import java.time.LocalDate

data class ReportsUiState(
    val calculation: ReportCalculation = ReportCalculation(),
    val referenceDate: LocalDate = LocalDate.now(),
    val period: ReportPeriod = ReportPeriod.Month,
    val firstMonth: Int = referenceDate.monthValue,
    val lastMonth: Int = referenceDate.monthValue,
    val selectedPoint: Int = 0,
    val format: ReportFormat = ReportFormat.Pdf,
    val customDialog: Boolean = false,
    val notice: ReportsNotice? = null,
    val pendingExport: ReportExport? = null,
    val exporting: Boolean = false,
    val loading: Boolean = true,
    val error: Boolean = false,
    val saving: Boolean = false,
    val dialog: ReportDialog? = null,
    val movements: List<RecordedMovement> = emptyList(),
    val settings: ReportSettings = ReportSettings(),
) {
    val report: FinancialReport get() {
        val start = when (period) {
            ReportPeriod.Month -> referenceDate.withDayOfMonth(1)
            ReportPeriod.Quarter -> referenceDate.minusMonths(2).withDayOfMonth(1)
            ReportPeriod.Year -> referenceDate.withDayOfYear(1)
            ReportPeriod.Custom -> referenceDate.withMonth(firstMonth).withDayOfMonth(1)
        }
        val end = if (period == ReportPeriod.Custom) referenceDate.withMonth(lastMonth).let { it.withDayOfMonth(it.lengthOfMonth()) } else referenceDate
        return calculation.get(movements, settings, start, end)
    }
}
enum class ReportDialog { Expense, Subscription }
enum class ReportsNotice { ExportSaved, ExportError, SaveError }

/** Retained across UI-only state copies, so opening menus does not rebuild the report. */
class ReportCalculation {
    @Volatile private var entries: List<Pair<List<Any>, FinancialReport>> = emptyList()
    fun get(movements: List<RecordedMovement>, settings: ReportSettings, start: LocalDate, end: LocalDate): FinancialReport {
        val key = listOf(movements, settings, start, end)
        entries.firstOrNull { it.first == key }?.let { return it.second }
        val report = buildReport(movements, settings, start, end)
        synchronized(this) { entries = (listOf(key to report) + entries).take(2) }
        return report
    }
}

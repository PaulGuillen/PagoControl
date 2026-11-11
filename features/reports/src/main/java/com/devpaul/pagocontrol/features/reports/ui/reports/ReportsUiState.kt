package com.devpaul.pagocontrol.features.reports.ui.reports

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.Month,
    val firstMonth: Int = 11,
    val lastMonth: Int = 11,
    val selectedPoint: Int = 2,
    val format: ReportFormat = ReportFormat.Pdf,
    val customDialog: Boolean = false,
    val notice: ReportsNotice? = null,
    val pendingExport: ReportExport? = null,
    val exporting: Boolean = false,
) {
    val report: FinancialReport get() = demoReport(firstMonth, lastMonth)
}

enum class ReportsNotice { Notifications, ExportSaved, ExportError }

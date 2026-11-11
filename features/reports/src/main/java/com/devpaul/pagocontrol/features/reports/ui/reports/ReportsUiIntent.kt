package com.devpaul.pagocontrol.features.reports.ui.reports

sealed interface ReportsUiIntent {
    data class Period(val value: ReportPeriod) : ReportsUiIntent
    data class CustomRange(val firstMonth: Int, val lastMonth: Int) : ReportsUiIntent
    data class Point(val index: Int) : ReportsUiIntent
    data class Format(val value: ReportFormat) : ReportsUiIntent
    data class Export(val uri: String?) : ReportsUiIntent
    data object PrepareExport : ReportsUiIntent
    data object Dismiss : ReportsUiIntent
    data object Notifications : ReportsUiIntent
}

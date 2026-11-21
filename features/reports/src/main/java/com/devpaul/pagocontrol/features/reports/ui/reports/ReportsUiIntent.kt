package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.ManualExpense
import com.devpaul.pagocontrol.domain.SubscriptionRule

sealed interface ReportsUiIntent {
    data object DismissNotice : ReportsUiIntent
    data object Retry : ReportsUiIntent
    data class Open(val dialog: ReportDialog) : ReportsUiIntent
    data class SaveExpense(val expense: ManualExpense) : ReportsUiIntent
    data class DeleteExpense(val id: String) : ReportsUiIntent
    data class AddCategory(val name: String) : ReportsUiIntent
    data class Category(val id: String, val category: ExpenseCategory) : ReportsUiIntent
    data class AddEmailSubscription(val movementId: String, val name: String) : ReportsUiIntent
    data class SubscriptionVisibility(val name: String, val hidden: Boolean) : ReportsUiIntent
    data class SaveSubscription(val rule: SubscriptionRule) : ReportsUiIntent
    data class DeleteSubscription(val id: String) : ReportsUiIntent
    data class Period(val value: ReportPeriod) : ReportsUiIntent
    data class CustomRange(val firstMonth: Int, val lastMonth: Int) : ReportsUiIntent
    data class Point(val index: Int) : ReportsUiIntent
    data class Format(val value: ReportFormat) : ReportsUiIntent
    data class Export(val uri: String?) : ReportsUiIntent
    data object PrepareExport : ReportsUiIntent
    data object Dismiss : ReportsUiIntent
}

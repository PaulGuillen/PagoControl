package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.ui.graphics.Color
import com.devpaul.pagocontrol.domain.ExpenseCategory

internal val reportChartPalette = listOf(Color(0xFF2979C7), Color(0xFFE29A19), Color(0xFF2B967A), Color(0xFF9262BA), Color(0xFFD76268), Color(0xFF687B8D))

internal fun ExpenseCategory.chartColor(categories: List<ExpenseCategory>): Color {
    val index = categories.indexOf(this).coerceAtLeast(0)
    return reportChartPalette[index % reportChartPalette.size]
}

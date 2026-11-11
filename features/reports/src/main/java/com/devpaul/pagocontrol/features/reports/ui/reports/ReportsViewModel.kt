package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.features.reports.export.ReportExporter
import com.devpaul.pagocontrol.platform.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ReportsViewModel(private val exporter: ReportExporter) : BaseViewModel<ReportsUiState>(ReportsUiState()) {
    fun onIntent(intent: ReportsUiIntent) {
        when (intent) {
            is ReportsUiIntent.Period -> updateState {
                when (intent.value) {
                    ReportPeriod.Month -> it.copy(period = intent.value, firstMonth = 11, lastMonth = 11, selectedPoint = 2)
                    ReportPeriod.Quarter -> it.copy(period = intent.value, firstMonth = 9, lastMonth = 11, selectedPoint = 2)
                    ReportPeriod.Year -> it.copy(period = intent.value, firstMonth = 1, lastMonth = 11, selectedPoint = 10)
                    ReportPeriod.Custom -> it.copy(customDialog = true)
                }
            }
            is ReportsUiIntent.CustomRange -> if (intent.firstMonth in 1..11 && intent.lastMonth in intent.firstMonth..11) updateState {
                it.copy(period = ReportPeriod.Custom, firstMonth = intent.firstMonth, lastMonth = intent.lastMonth, selectedPoint = 0, customDialog = false)
            }
            is ReportsUiIntent.Point -> if (intent.index in state.value.report.points.indices) updateState { it.copy(selectedPoint = intent.index) }
            is ReportsUiIntent.Format -> updateState { it.copy(format = intent.value) }
            ReportsUiIntent.PrepareExport -> if (state.value.pendingExport == null && !state.value.exporting) updateState { it.copy(pendingExport = ReportExport(it.report, it.format)) }
            is ReportsUiIntent.Export -> export(intent.uri)
            ReportsUiIntent.Dismiss -> updateState { it.copy(customDialog = false, notice = null) }
            ReportsUiIntent.Notifications -> updateState { it.copy(notice = ReportsNotice.Notifications) }
        }
    }

    private fun export(uri: String?) {
        if (state.value.exporting) return
        val pending = state.value.pendingExport ?: return
        if (uri == null) { updateState { it.copy(pendingExport = null) }; return }
        updateState { it.copy(exporting = true) }
        viewModelScope.launch {
            try {
                exporter.write(uri, pending)
                updateState { it.copy(notice = ReportsNotice.ExportSaved) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(notice = ReportsNotice.ExportError) }
            } finally {
                updateState { it.copy(exporting = false, pendingExport = null) }
            }
        }
    }
}

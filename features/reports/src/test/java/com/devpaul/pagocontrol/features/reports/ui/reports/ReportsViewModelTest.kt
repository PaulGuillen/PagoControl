package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.features.reports.export.ReportExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun periodsAndPointSelectionStayWithinRange() {
        val viewModel = ReportsViewModel(MemoryExporter())
        viewModel.onIntent(ReportsUiIntent.Period(ReportPeriod.Year))
        assertEquals(11, viewModel.state.value.report.points.size)
        viewModel.onIntent(ReportsUiIntent.Point(99))
        assertEquals(10, viewModel.state.value.selectedPoint)
        viewModel.onIntent(ReportsUiIntent.Period(ReportPeriod.Custom))
        viewModel.onIntent(ReportsUiIntent.CustomRange(10, 8))
        assertEquals(ReportPeriod.Year, viewModel.state.value.period)
        viewModel.onIntent(ReportsUiIntent.CustomRange(7, 8))
        assertEquals(2, viewModel.state.value.report.points.size)
        assertEquals(0, viewModel.state.value.selectedPoint)
        assertFalse(viewModel.state.value.customDialog)
    }

    @Test fun exportUsesSnapshotAndPreventsDuplicateWrites() = runTest {
        val exporter = MemoryExporter()
        val viewModel = ReportsViewModel(exporter)
        viewModel.onIntent(ReportsUiIntent.Format(ReportFormat.Excel))
        viewModel.onIntent(ReportsUiIntent.PrepareExport)
        viewModel.onIntent(ReportsUiIntent.Period(ReportPeriod.Quarter))
        viewModel.onIntent(ReportsUiIntent.Export("content://documents/report"))
        viewModel.onIntent(ReportsUiIntent.Export("content://documents/report"))
        advanceUntilIdle()
        assertEquals(1, exporter.exports.size)
        assertEquals(11, exporter.exports.single().report.firstMonth)
        assertEquals(ReportFormat.Excel, exporter.exports.single().format)
        assertEquals(ReportsNotice.ExportSaved, viewModel.state.value.notice)
        assertNull(viewModel.state.value.pendingExport)
    }

    @Test fun canceledPickerDoesNotWriteAndFailureAllowsRetry() = runTest {
        val exporter = MemoryExporter()
        val viewModel = ReportsViewModel(exporter)
        viewModel.onIntent(ReportsUiIntent.PrepareExport)
        viewModel.onIntent(ReportsUiIntent.Export(null))
        assertNull(viewModel.state.value.pendingExport)
        assertEquals(0, exporter.exports.size)
        exporter.fail = true
        viewModel.onIntent(ReportsUiIntent.PrepareExport)
        viewModel.onIntent(ReportsUiIntent.Export("content://documents/report"))
        advanceUntilIdle()
        assertEquals(ReportsNotice.ExportError, viewModel.state.value.notice)
        assertFalse(viewModel.state.value.exporting)
        exporter.fail = false
        viewModel.onIntent(ReportsUiIntent.Dismiss)
        viewModel.onIntent(ReportsUiIntent.PrepareExport)
        viewModel.onIntent(ReportsUiIntent.Export("content://documents/report"))
        advanceUntilIdle()
        assertEquals(ReportsNotice.ExportSaved, viewModel.state.value.notice)
    }

    private class MemoryExporter : ReportExporter {
        val exports = mutableListOf<ReportExport>()
        var fail = false

        override suspend fun write(uri: String, export: ReportExport) {
            if (fail) error("Cannot write document")
            exports += export
        }
    }
}

package com.devpaul.pagocontrol.features.reports.export

import com.devpaul.pagocontrol.features.reports.ui.reports.ReportExport

interface ReportExporter {
    suspend fun write(uri: String, export: ReportExport)
}

package com.devpaul.pagocontrol

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.features.reports.export.AndroidReportExporter
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportExport
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportFormat
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportsUiState
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportsExportTest {
    @Test fun writesValidPdfExcelAndCsvDocuments() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val exporter = AndroidReportExporter(context)
        ReportFormat.entries.forEach { format ->
            val file = java.io.File(context.cacheDir, "reports-export-test.${format.extension}")
            try {
                exporter.write(Uri.fromFile(file).toString(), ReportExport(ReportsUiState().report, format))
                assertTrue(file.length() > 100)
                when (format) {
                    ReportFormat.Pdf -> assertEquals("%PDF-", file.inputStream().use { stream ->
                        val header = ByteArray(5)
                        stream.read(header)
                        String(header, Charsets.US_ASCII)
                    })
                    ReportFormat.Csv -> {
                        val contents = file.readText()
                        assertTrue(contents.contains("4580.50"))
                        assertTrue(contents.contains("Datos de demostración"))
                    }
                    ReportFormat.Excel -> ZipFile(file).use { zip ->
                        val entries = zip.entries().toList()
                        assertEquals(5, entries.size)
                        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        entries.forEach { entry -> zip.getInputStream(entry).use { parser.parse(it) } }
                        val sheet = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().use { it.readText() }
                        assertTrue(sheet.contains("<v>4580.50</v>"))
                    }
                }
            } finally {
                file.delete()
            }
        }
    }
}

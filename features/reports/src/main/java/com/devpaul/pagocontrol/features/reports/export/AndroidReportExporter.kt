package com.devpaul.pagocontrol.features.reports.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.devpaul.pagocontrol.features.reports.R
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportExport
import com.devpaul.pagocontrol.features.reports.ui.reports.ReportFormat
import com.devpaul.pagocontrol.features.reports.ui.reports.demoSubscriptions
import java.io.OutputStream
import java.math.BigDecimal
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory(binds = [ReportExporter::class])
class AndroidReportExporter(private val context: Context) : ReportExporter {
    override suspend fun write(uri: String, export: ReportExport) = withContext(Dispatchers.IO) {
        val report = export.report
        val months = context.resources.getStringArray(R.array.reports_months)
        val rows = listOf(
            listOf("PagoControl", context.getString(R.string.reports_demo_export)),
            listOf(context.getString(R.string.reports_period_label), "${months[report.firstMonth - 1]} – ${months[report.lastMonth - 1]} 2023"),
            listOf(context.getString(R.string.reports_currency), "PEN"),
            listOf(context.getString(R.string.reports_income), decimal(report.income)),
            listOf(context.getString(R.string.reports_expense), decimal(report.expense)),
            listOf(context.getString(R.string.reports_net_balance), decimal(report.net)),
            listOf(context.getString(R.string.reports_categories), ""),
        ) + report.categories.map { listOf(context.getString(it.label), decimal(it.cents)) } +
            listOf(listOf(context.getString(R.string.reports_channels), "")) +
            report.channels.map { listOf(context.getString(it.label), decimal(it.cents)) } +
            listOf(listOf(context.getString(R.string.reports_subscriptions), "")) +
            demoSubscriptions
                .map { listOf(it.first, decimal(it.second * (report.lastMonth - report.firstMonth + 1))) } +
            listOf(listOf(context.getString(R.string.reports_export_note), ""))
        val stream = context.contentResolver.openOutputStream(Uri.parse(uri), "wt") ?: error("Cannot open document")
        stream.use { output ->
            when (export.format) {
                ReportFormat.Pdf -> writePdf(output, rows)
                ReportFormat.Csv -> output.write(("\uFEFF" + rows.joinToString("\r\n") { row ->
                    row.joinToString(";") { value -> "\"" + value.replace("\"", "\"\"") + "\"" }
                }).toByteArray(Charsets.UTF_8))
                ReportFormat.Excel -> writeExcel(output, rows)
            }
        }
    }

    private fun decimal(cents: Long) = BigDecimal.valueOf(cents, 2).toPlainString()

    private fun writePdf(output: OutputStream, rows: List<List<String>>) {
        val document = PdfDocument()
        try {
            val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(244, 197, 66)
            page.canvas.drawRect(0f, 0f, 595f, 90f, paint)
            paint.color = Color.rgb(23, 26, 22)
            paint.textSize = 22f
            paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            page.canvas.drawText(context.getString(R.string.reports_title), 32f, 54f, paint)
            paint.textSize = 11f
            paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            rows.forEachIndexed { index, row ->
                val baseline = 120f + index * 23f
                page.canvas.drawText(row[0], 32f, baseline, paint)
                paint.textAlign = Paint.Align.RIGHT
                page.canvas.drawText(row[1], 563f, baseline, paint)
                paint.textAlign = Paint.Align.LEFT
            }
            document.finishPage(page)
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    private fun writeExcel(output: OutputStream, rows: List<List<String>>) {
        ZipOutputStream(output).use { zip ->
            fun entry(name: String, contents: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(contents.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>""".trimIndent())
            entry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            entry("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="PagoControl" sheetId="1" r:id="rId1"/></sheets></workbook>""")
            entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
            val cells = rows.mapIndexed { index, row ->
                val rowNumber = index + 1
                val content = row.mapIndexed { column, value ->
                    val reference = "${if (column == 0) "A" else "B"}$rowNumber"
                    if (column == 1 && value.toBigDecimalOrNull() != null) """<c r="$reference"><v>$value</v></c>"""
                    else """<c r="$reference" t="inlineStr"><is><t xml:space="preserve">${escapeXml(value)}</t></is></c>"""
                }.joinToString("")
                """<row r="$rowNumber">$content</row>"""
            }.joinToString("")
            entry("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><cols><col min="1" max="1" width="64" customWidth="1"/><col min="2" max="2" width="38" customWidth="1"/></cols><sheetData>$cells</sheetData></worksheet>""")
        }
    }

    private fun escapeXml(value: String) = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}

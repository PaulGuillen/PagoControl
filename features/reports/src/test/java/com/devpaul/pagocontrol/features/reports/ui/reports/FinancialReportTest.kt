package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.ManualExpense
import com.devpaul.pagocontrol.domain.RecordedMovement
import com.devpaul.pagocontrol.domain.ReportSettings
import com.devpaul.pagocontrol.domain.SubscriptionRule
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialReportTest {
    private val zone = ZoneOffset.UTC
    private val start = LocalDate.of(2026, 11, 1)
    private val end = LocalDate.of(2026, 11, 30)
    private fun movement(id: String, title: String, cents: Long, day: Int = 20, text: String = "") = RecordedMovement(id, title, "Gmail", "com.google.android.gm", cents, start.withDayOfMonth(day).atStartOfDay(zone).toInstant().toEpochMilli(), "BCP Notificaciones", text)

    @Test fun flowListCombinesBothDirectionsByDateWithoutDroppingManualExpenses() {
        val settings = ReportSettings(expenses = listOf(
            ManualExpense("same", "Gasto manual", 2000, "2026-11-20", "Yape", ExpenseCategory.Other),
        ))
        val report = buildReport(listOf(
            movement("same", "Abono", 8000, 20),
            movement("out", "Compra", -1000, 20),
            movement("older", "Abono anterior", 3000, 2),
        ), settings, start, end, zone)
        val grouped = report.flowMovements()
        assertEquals(listOf(start.withDayOfMonth(20), start.withDayOfMonth(2)), grouped.keys.toList())
        assertEquals(3, grouped.getValue(start.withDayOfMonth(20)).size)
        val rows = grouped.values.flatten()
        assertEquals(4, rows.map { it.key }.distinct().size)
        assertEquals(report.net, rows.sumOf { it.cents })
        assertTrue(grouped.getValue(start.withDayOfMonth(20)).any { it.cents > 0 })
        assertTrue(grouped.getValue(start.withDayOfMonth(20)).any { it.cents < 0 })
        assertEquals(1, report.flowMovements("Yape").values.flatten().size)
    }

    @Test fun customCategoriesAndGamesKeepSeparateTotalsAfterClassification() {
        val custom = ExpenseCategory.custom("  Mascotas  ")
        val restored = ExpenseCategory.valueOf(custom.name)
        assertEquals(custom, restored)
        assertEquals("Mascotas", restored.customName)
        val settings = ReportSettings(
            categories = mapOf("pet" to restored, "game" to ExpenseCategory.Games),
            customCategories = listOf(restored),
            expenses = listOf(ManualExpense("manual", "Veterinario", 5000, "2026-11-20", "Yape", restored)),
        )
        val report = buildReport(listOf(movement("pet", "Comida para mascota", -2000), movement("game", "Juego", -3000)), settings, start, end, zone)
        assertEquals(7000L, report.categories.single { it.category == restored }.cents)
        assertEquals(3000L, report.categories.single { it.category == ExpenseCategory.Games }.cents)
        assertEquals(report.expense, report.categories.sumOf { it.cents })
        assertEquals(5000L, report.channels.single { it.source == "Yape" }.expense)
        assertEquals(ExpenseCategory.Food, ExpenseCategory.valueOf("Food"))
    }

    @Test fun addSubscriptionUsesActualGmailPaymentsNotAlreadyListed() {
        val records = listOf(
            movement("paid", "Netflix", -4490),
            movement("hidden", "Spotify", -2090),
            movement("new", "Mi gimnasio", -8000),
            movement("new-repeat", "Mi gimnasio", -8000),
            movement("income", "Apple TV+", 5000),
            movement("wallet", "Canva", -1500).copy(packageName = "wallet.app"),
        )
        val settings = ReportSettings(hiddenSubscriptions = setOf("Spotify"))
        val report = buildReport(records, settings, start, end, zone)
        assertEquals(setOf("Spotify", "Mi gimnasio"), availableEmailSubscriptions(records, report, settings).map { it.title }.toSet())
        val added = settings.copy(categories = mapOf("new" to ExpenseCategory.Subscriptions), hiddenSubscriptions = emptySet())
        val updated = buildReport(records, added, start, end, zone)
        assertTrue(availableEmailSubscriptions(records, updated, added).isEmpty())
        assertEquals(report.expense, updated.expense)
    }

    @Test fun mappedServicesAppearOnlyWhenPaidOrExplicitlySelected() {
        val report = buildReport(emptyList(), ReportSettings(), start, end, zone)
        assertTrue(subscriptionList(report, ReportSettings()).isEmpty())
        val selected = ReportSettings(rules = listOf(SubscriptionRule("spotify", "Spotify", "spotify")))
        assertEquals("Spotify", subscriptionList(report, selected).single().name)
        assertEquals(0L, subscriptionList(report, selected).single().cents)
    }

    @Test fun incomeAndExpensesStaySeparateByChannelAndSelectedWeek() {
        val report = buildReport(listOf(movement("in-1", "Abono", 10000, 2), movement("in-2", "Abono", 5000, 15), movement("out-1", "Compra", -3000, 2)), ReportSettings(), start, end, zone)
        assertEquals(15000L, report.incomes.sumOf { it.cents })
        assertEquals(3000L, report.expenses.sumOf { it.cents })
        assertEquals(15000L, report.channels.sumOf { it.income })
        assertEquals(3000L, report.channels.sumOf { it.expense })
        assertEquals(10000L, report.incomes.filter { report.matchesPoint(it.date, 0) }.sumOf { it.cents })
        assertEquals(5000L, report.incomes.filter { report.matchesPoint(it.date, 2) }.sumOf { it.cents })
        assertTrue(report.incomes.all { it.cents > 0 })
        assertEquals(18000L, report.channels.sumOf { it.cents })
    }

    @Test fun mappedServicesStayOutOfPersonalListAndRemovingOnePreservesRealSpending() {
        val report = buildReport(listOf(movement("netflix", "Netflix", -4490)), ReportSettings(), start, end, zone)
        val defaults = subscriptionList(report, ReportSettings())
        assertTrue(defaults.none { it.name == "Spotify" })
        assertTrue(SubscriptionCatalog.services.any { it.name == "Spotify" })
        assertEquals(4490L, defaults.first { it.name == "Netflix" }.cents)
        val hidden = ReportSettings(hiddenSubscriptions = setOf("Netflix"))
        assertTrue(subscriptionList(report, hidden).none { it.name == "Netflix" })
        assertEquals(4490L, report.subscriptions.sumOf { it.cents })
        assertTrue(subscriptionList(report, hidden.copy(hiddenSubscriptions = emptySet())).any { it.name == "Netflix" })
    }

    @Test fun openingMenusAndSavingDoNotRebuildReportButCategoryChangesDo() {
        val state = ReportsUiState(loading = false)
        val report = state.report
        assertSame(report, state.copy(saving = true, dialog = ReportDialog.Subscription).report)
        assertNotSame(report, state.copy(settings = ReportSettings(categories = mapOf("one" to ExpenseCategory.Food))).report)
    }

    @Test fun reportBalancesCategoriesChannelsAndPointsWithManualExpenses() {
        val movements = listOf(movement("income", "Abono", 100000), movement("crunchy", "BCP - EBN*CRUNCHYROLL", -1990), movement("yape", "Pago", -28290, text = "Tu yapeo de servicio ha sido confirmado"))
        val manual = ManualExpense("lunch", "Almuerzo", 1500, "2026-11-21", "Efectivo", ExpenseCategory.Food)
        val report = buildReport(movements, ReportSettings(expenses = listOf(manual)), start, end, zone)
        assertEquals(100000L, report.income)
        assertEquals(31780L, report.expense)
        assertEquals(report.expense, report.categories.sumOf { it.cents })
        assertEquals(report.expense, report.points.sumOf { it.expense })
        assertEquals(report.income, report.points.sumOf { it.income })
        assertEquals(report.expense + report.income, report.channels.sumOf { it.cents })
        assertEquals(4, report.channels.sumOf { it.operations })
        assertEquals("Crunchyroll", report.subscriptions.single().name)
        assertEquals(1990L, report.subscriptions.single().cents)
        assertEquals(ExpenseCategory.Services, report.expenses.find { it.id == "yape" }!!.category)
    }

    @Test fun catalogRecognizesDescriptorsAndAvoidsGenericMarketplaces() {
        mapOf("EBN*CRUNCHYROLL" to "Crunchyroll", "GOOGLE *YouTube" to "YouTube Premium", "NETFLIX.COM" to "Netflix", "SPOTIFY P123" to "Spotify", "Disney Plus" to "Disney+", "HBO MAX" to "HBO Max", "Google *Storage" to "Google One").forEach { (descriptor, name) -> assertEquals(name, SubscriptionCatalog.detect(descriptor)) }
        listOf("AMAZON compra", "APPLE.COM/BILL", "GOOGLE PLAY", "MAX supermercado", "Netflixera").forEach { assertNull(SubscriptionCatalog.detect(it)) }
        assertTrue(SubscriptionCatalog.services.size >= 40)
    }

    @Test fun customRulesApplyRetroactivelyAndCategoryOverrideWins() {
        val movements = listOf(movement("custom", "FIT*GYMCLUB", -8000), movement("stream", "Netflix", -4490))
        val settings = ReportSettings(rules = listOf(SubscriptionRule("gym", "Gimnasio", "gymclub")), categories = mapOf("stream" to ExpenseCategory.Other))
        val report = buildReport(movements, settings, start, end, zone)
        assertEquals("Gimnasio", report.subscriptions.single().name)
        assertEquals(8000L, report.subscriptions.single().cents)
        assertEquals(ExpenseCategory.Other, report.expenses.find { it.id == "stream" }!!.category)
    }

    @Test fun userCanClassifyAnUnknownServiceAsSubscription() {
        val report = buildReport(listOf(movement("unknown", "Mi gimnasio", -8000)), ReportSettings(categories = mapOf("unknown" to ExpenseCategory.Subscriptions)), start, end, zone)
        assertEquals("Mi gimnasio", report.subscriptions.single().name)
        assertEquals(report.expense, report.subscriptions.sumOf { it.cents })
    }

    @Test fun repeatedChargesSumButDuplicateIdsAndIncomeAreNotSubscriptions() {
        val first = movement("one", "Netflix", -4490)
        val report = buildReport(listOf(first, first, movement("two", "Netflix", -4490), movement("refund", "Netflix", 4490)), ReportSettings(), start, end, zone)
        assertEquals(8980L, report.subscriptions.single().cents)
        assertEquals(2, report.subscriptions.single().payments)
        assertEquals(4490L, report.income)
    }

    @Test fun emptyReportHasZeroTotalsAndSafePoints() {
        val report = buildReport(emptyList(), ReportSettings(), start, end, zone)
        assertEquals(0L, report.expense)
        assertTrue(report.channels.isEmpty())
        assertTrue(report.points.isNotEmpty())
        assertTrue(report.categories.all { it.cents == 0L })
    }

    @Test fun dateRangeIncludesBoundariesAndExcludesOutsideMovements() {
        val items = listOf(movement("first", "Netflix", -100, 1), movement("last", "Netflix", -200, 30), movement("outside", "Netflix", -300).copy(postedAt = start.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()))
        assertEquals(300L, buildReport(items, ReportSettings(), start, end, zone).expense)
    }

    @Test fun quarterSpansYearBoundaryAndRetainsEveryCent() {
        val state = ReportsUiState(referenceDate = LocalDate.of(2027, 1, 15), period = ReportPeriod.Quarter)
        assertEquals(LocalDate.of(2026, 11, 1), state.report.start)
        assertEquals(listOf(11, 12, 1), state.report.points.map { it.number })
    }

    @Test fun validatesAmountsWithoutRoundingOrAcceptingNegativeValues() {
        assertEquals(8490L, expenseCents("84,90"))
        assertEquals(1990L, expenseCents("19.90"))
        listOf("0", "-10", "2.999", "1e4", "", "NaN", "1,000.00").forEach { assertNull(expenseCents(it)) }
    }
}

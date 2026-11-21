package com.devpaul.pagocontrol.features.reports.ui.reports

import com.devpaul.pagocontrol.features.reports.export.ReportExporter
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.ManualExpense
import com.devpaul.pagocontrol.domain.MovementHistoryRepository
import com.devpaul.pagocontrol.domain.RecordedMovement
import com.devpaul.pagocontrol.domain.ReportRepository
import com.devpaul.pagocontrol.domain.ReportSettings
import com.devpaul.pagocontrol.domain.SubscriptionRule
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun periodsAndPointSelectionStayWithinRange() {
        val viewModel = createViewModel(MemoryExporter())
        viewModel.onIntent(ReportsUiIntent.Period(ReportPeriod.Year))
        assertEquals(LocalDate.now().monthValue, viewModel.state.value.report.points.size)
        viewModel.onIntent(ReportsUiIntent.Point(99))
        assertEquals(0, viewModel.state.value.selectedPoint)
        viewModel.onIntent(ReportsUiIntent.Period(ReportPeriod.Custom))
        viewModel.onIntent(ReportsUiIntent.CustomRange(10, 8))
        assertEquals(ReportPeriod.Year, viewModel.state.value.period)
        viewModel.onIntent(ReportsUiIntent.CustomRange(7, 8))
        assertEquals(2, viewModel.state.value.report.points.size)
        assertEquals(0, viewModel.state.value.selectedPoint)
        assertFalse(viewModel.state.value.customDialog)
    }

    @Test
    fun exportUsesSnapshotAndPreventsDuplicateWrites() = runTest {
        val exporter = MemoryExporter()
        val viewModel = createViewModel(exporter)
        advanceUntilIdle()
        viewModel.onIntent(ReportsUiIntent.Format(ReportFormat.Excel))
        viewModel.onIntent(ReportsUiIntent.PrepareExport)
        viewModel.onIntent(ReportsUiIntent.Period(ReportPeriod.Quarter))
        viewModel.onIntent(ReportsUiIntent.Export("content://documents/report"))
        viewModel.onIntent(ReportsUiIntent.Export("content://documents/report"))
        advanceUntilIdle()
        assertEquals(1, exporter.exports.size)
        assertEquals(LocalDate.now().monthValue, exporter.exports.single().report.firstMonth)
        assertEquals(ReportFormat.Excel, exporter.exports.single().format)
        assertEquals(ReportsNotice.ExportSaved, viewModel.state.value.notice)
        assertNull(viewModel.state.value.pendingExport)
    }

    @Test
    fun canceledPickerDoesNotWriteAndFailureAllowsRetry() = runTest {
        val exporter = MemoryExporter()
        val viewModel = createViewModel(exporter)
        advanceUntilIdle()
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

    private fun createViewModel(exporter: ReportExporter, repository: MemoryReports = MemoryReports(), auth: TestAuth = TestAuth()): ReportsViewModel = ReportsViewModel(exporter, object : MovementHistoryRepository {
        override fun observeMovementHistory(): Flow<List<RecordedMovement>> = flowOf(emptyList())
    }, repository, auth)

    @Test
    fun manualExpensesPersistAndAccountSwitchClearsVisibleData() = runTest {
        val repository = MemoryReports()
        val auth = TestAuth()
        val viewModel = createViewModel(MemoryExporter(), repository, auth)
        advanceUntilIdle()
        val expense = ManualExpense("one", "Netflix", 4490, LocalDate.now().toString(), "BCP", ExpenseCategory.Subscriptions)
        viewModel.onIntent(ReportsUiIntent.SaveExpense(expense))
        advanceUntilIdle()
        viewModel.state.first { !it.loading && it.report.expense == 4490L }
        assertEquals(4490L, viewModel.state.value.report.expense)
        assertEquals("first", repository.savedUser)
        auth.session.value = UserSession("second", "Other", "other@example.com")
        advanceUntilIdle()
        viewModel.state.first { !it.loading && it.report.expense == 0L }
        assertEquals(0L, viewModel.state.value.report.expense)
    }

    private class TestAuth : AuthRepository {
        override val session = MutableStateFlow<UserSession?>(UserSession("first", "User", "user@example.com"))
        override suspend fun signIn(email: String, password: String): SignInResult = error("Unused")
        override suspend fun signOut() { session.value = null }
    }
    private class MemoryReports : ReportRepository {
        override suspend fun addCategory(userId: String, category: ExpenseCategory) { data(userId).value = data(userId).value.copy(customCategories = data(userId).value.customCategories + category) }
        private val users = mutableMapOf<String, MutableStateFlow<ReportSettings>>()
        var savedUser: String? = null
        private fun data(userId: String) = users.getOrPut(userId) { MutableStateFlow(ReportSettings()) }
        override fun observe(userId: String): Flow<ReportSettings> = data(userId)
        override suspend fun saveExpense(userId: String, expense: ManualExpense) {
            savedUser = userId
            data(userId).value = data(userId).value.copy(expenses = listOf(expense))
        }
        override suspend fun removeExpense(userId: String, id: String) { data(userId).value = data(userId).value.copy(expenses = emptyList()) }
        override suspend fun setCategory(userId: String, movementId: String, category: ExpenseCategory) { data(userId).value = data(userId).value.copy(categories = mapOf(movementId to category)) }
        override suspend fun setSubscriptionHidden(userId: String, name: String, hidden: Boolean) { data(userId).value = data(userId).value.copy(hiddenSubscriptions = if (hidden) data(userId).value.hiddenSubscriptions + name else data(userId).value.hiddenSubscriptions - name) }
        override suspend fun saveRule(userId: String, rule: SubscriptionRule) { data(userId).value = data(userId).value.copy(rules = listOf(rule)) }
        override suspend fun removeRule(userId: String, id: String) { data(userId).value = data(userId).value.copy(rules = emptyList()) }
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

package com.devpaul.pagocontrol.features.movements.ui.movements

import com.devpaul.pagocontrol.domain.MovementHistoryRepository
import com.devpaul.pagocontrol.domain.RecordedMovement
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovementsViewModelTest {
    private val today = LocalDate.now()
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class FakeHistory : MovementHistoryRepository {
        val records = MutableStateFlow<List<RecordedMovement>>(emptyList())
        override fun observeMovementHistory(): Flow<List<RecordedMovement>> = records
    }

    private fun record(id: String, amount: Long, gmail: Boolean = false, date: LocalDate = today, hour: Int = 12) = RecordedMovement(
        id = id,
        title = "Movement $id",
        source = if (gmail) "BCP" else "Yape",
        packageName = if (gmail) "com.google.android.gm" else "com.bcp.innovacxion.yapeapp",
        amountCents = amount,
        postedAt = date.atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        notificationTitle = "Original title $id",
        notificationText = "Original body $id",
    )

    @Test fun observesRealRecordsAndUpdatesMonthlyBalancesFromBothChannels() = runTest(dispatcher) {
        val repo = FakeHistory()
        val vm = MovementsViewModel(repo)
        repo.records.value = listOf(record("wallet", 50000), record("mail", -2000, true))
        assertEquals(MonthlyMovementBalance(50000, 0), vm.state.value.balance(MovementChannel.Wallet))
        assertEquals(MonthlyMovementBalance(0, -2000), vm.state.value.balance(MovementChannel.Gmail))
        assertFalse(vm.state.value.loading)
        repo.records.value = repo.records.value + record("new", 1500, true)
        assertEquals(3, vm.state.value.movements.size)
        assertEquals(-500L, vm.state.value.balance(MovementChannel.Gmail).net)
        // An empty account feed must clear the previous account data and open details.
        vm.onIntent(MovementsUiIntent.Review(vm.state.value.movements.first()))
        repo.records.value = emptyList()
        assertTrue(vm.state.value.visible.isEmpty())
        assertEquals(null, vm.state.value.reviewing)
        assertEquals(0L, vm.state.value.balance(MovementChannel.Wallet).net)
    }

    @Test fun monthChannelAndSearchFiltersCombineWithoutChangingMonthlyTotals() = runTest(dispatcher) {
        val repo = FakeHistory()
        val vm = MovementsViewModel(repo)
        val previousMonth = YearMonth.now().minusMonths(1)
        repo.records.value = listOf(record("wallet", 8500), record("gmail", 10000, true), record("old", 3000, date = previousMonth.atDay(1)))
        vm.onIntent(MovementsUiIntent.Channel(MovementChannel.Wallet))
        vm.onIntent(MovementsUiIntent.Search("85"))
        assertEquals(listOf("wallet"), vm.state.value.visible.map { it.id })
        assertEquals(8500L, vm.state.value.balance(MovementChannel.Wallet).income)
        vm.onIntent(MovementsUiIntent.Channel(MovementChannel.Gmail))
        assertTrue(vm.state.value.visible.isEmpty())
        vm.onIntent(MovementsUiIntent.Reset)
        vm.onIntent(MovementsUiIntent.Month(previousMonth))
        vm.onIntent(MovementsUiIntent.ViewAll)
        assertEquals(listOf("old"), vm.state.value.visible.map { it.id })
        assertEquals(3000L, vm.state.value.balance(MovementChannel.Wallet).income)
    }

    @Test fun recentWindowUsesActualDatesAndCompleteHistorySortsByDateAndTime() {
        val reference = LocalDate.of(2026, 11, 15)
        val records = listOf(
            record("older", 100, date = reference.minusDays(3)),
            record("morning", 100, date = reference, hour = 8),
            record("future", 100, date = reference.plusDays(1)),
            record("afternoon", 100, date = reference, hour = 16),
            record("yesterday", 100, date = reference.minusDays(1)),
            record("boundary", 100, date = reference.minusDays(2)),
        )
        val state = MovementsUiState(month = YearMonth.from(reference), referenceDate = reference, movements = records.map { it.toHistoryMovement() })
        assertEquals(listOf("afternoon", "morning", "yesterday", "boundary"), state.visible.map { it.id })
        assertEquals(listOf("future", "afternoon", "morning", "yesterday", "boundary", "older"), state.copy(showAll = true).visible.map { it.id })
    }

    @Test fun recentPreviewShowsLatestThreePerDayWithoutLimitingHistoryOrMonthlyBalances() {
        val reference = LocalDate.of(2026, 11, 15)
        val records = (0..3).flatMap { daysAgo ->
            (8..12).map { hour ->
                record("$daysAgo-$hour", 100, date = reference.minusDays(daysAgo.toLong()), hour = hour)
            }
        }
        val state = MovementsUiState(
            month = YearMonth.from(reference),
            referenceDate = reference,
            movements = records.map { it.toHistoryMovement() },
        )
        assertEquals(9, state.visible.size)
        assertEquals(listOf("0-12", "0-11", "0-10", "1-12", "1-11", "1-10", "2-12", "2-11", "2-10"), state.visible.map { it.id })
        assertEquals(20, state.copy(showAll = true).visible.size)
        assertEquals(2000L, state.balance(MovementChannel.Wallet).income)
    }

    @Test fun paginationLoadsMoreOnlyInCompleteHistoryAndFiltersResetItsLimit() = runTest(dispatcher) {
        val repo = FakeHistory()
        val vm = MovementsViewModel(repo)
        repo.records.value = (1..45).map { record(it.toString(), 100) }
        vm.onIntent(MovementsUiIntent.LoadMore)
        assertEquals(20, vm.state.value.historyLimit)
        vm.onIntent(MovementsUiIntent.ViewAll)
        assertEquals(20, vm.state.value.visible.size)
        assertTrue(vm.state.value.hasMore)
        vm.onIntent(MovementsUiIntent.LoadMore)
        assertEquals(40, vm.state.value.visible.size)
        vm.onIntent(MovementsUiIntent.LoadMore)
        assertEquals(45, vm.state.value.visible.size)
        assertFalse(vm.state.value.hasMore)
        vm.onIntent(MovementsUiIntent.Search("Movement"))
        assertEquals(20, vm.state.value.visible.size)
        vm.onIntent(MovementsUiIntent.Back)
        assertFalse(vm.state.value.showAll)
        assertEquals("Movement", vm.state.value.query)
    }

    @Test fun preservesNotificationPayloadAndLocalDateForDetails() {
        val source = record("payload", 12345, true, hour = 23)
        val mapped = source.toHistoryMovement()
        assertEquals(source.notificationTitle, mapped.notificationTitle)
        assertEquals(source.notificationText, mapped.notificationText)
        assertEquals(MovementChannel.Gmail, mapped.channel)
        assertEquals(today, mapped.date)
        assertEquals(LocalTime.of(23, 0), mapped.time)
    }

    @Test fun failedLoadingCanBeRetriedWithoutShowingDemoData() = runTest(dispatcher) {
        var fail = true
        val repo = object : MovementHistoryRepository {
            override fun observeMovementHistory(): Flow<List<RecordedMovement>> = flow {
                if (fail) error("Unavailable")
                emit(listOf(record("recovered", 100)))
            }
        }
        val vm = MovementsViewModel(repo)
        assertTrue(vm.state.value.error)
        assertTrue(vm.state.value.visible.isEmpty())
        fail = false
        vm.onIntent(MovementsUiIntent.Retry)
        assertFalse(vm.state.value.error)
        assertEquals(listOf("recovered"), vm.state.value.visible.map { it.id })
    }
}

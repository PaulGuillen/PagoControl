package com.devpaul.pagocontrol.features.movements.ui.movements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovementsViewModelTest {
    @Test fun combinesSearchSourceAndCategory() {
        val viewModel = MovementsViewModel()
        viewModel.onIntent(MovementsUiIntent.Filter("income"))
        viewModel.onIntent(MovementsUiIntent.Source("Yape"))
        viewModel.onIntent(MovementsUiIntent.Search("MARÍA"))
        assertEquals(listOf("1"), viewModel.state.value.visible.map { it.id })
        viewModel.onIntent(MovementsUiIntent.Search("85"))
        assertEquals(listOf("1"), viewModel.state.value.visible.map { it.id })
        viewModel.onIntent(MovementsUiIntent.Source("BCP"))
        assertTrue(viewModel.state.value.visible.isEmpty())
    }

    @Test fun categorizingPendingRemovesItFromPendingFilter() {
        val viewModel = MovementsViewModel()
        viewModel.onIntent(MovementsUiIntent.Filter("pending"))
        val pending = viewModel.state.value.visible.single()
        viewModel.onIntent(MovementsUiIntent.Review(pending))
        viewModel.onIntent(MovementsUiIntent.Categorize("Ventas"))
        assertTrue(viewModel.state.value.visible.isEmpty())
        val categorized = viewModel.state.value.movements.single { it.id == pending.id }
        assertFalse(categorized.pending)
        assertEquals("Ventas", categorized.badge)
        assertEquals(pending.amountCents, categorized.amountCents)
        assertEquals(null, viewModel.state.value.reviewing)
    }

    @Test fun transfersStaySeparateFromIncomeAndExpense() {
        val state = MovementsUiState(filter = "transfer")
        assertEquals(listOf("6"), state.visible.map { it.id })
        assertFalse(state.copy(filter = "income").visible.any { it.kind == "transfer" })
        assertFalse(state.copy(filter = "expense").visible.any { it.kind == "transfer" })
    }

    @Test fun emptyMonthAndResetRestoreHistory() {
        val viewModel = MovementsViewModel()
        viewModel.onIntent(MovementsUiIntent.Month("Octubre 2023"))
        assertTrue(viewModel.state.value.visible.isEmpty())
        viewModel.onIntent(MovementsUiIntent.Reset)
        assertEquals(8, viewModel.state.value.visible.size)
    }
}

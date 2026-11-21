package com.devpaul.pagocontrol.features.movements.ui.movements

import java.time.LocalDate
import java.time.YearMonth

enum class MovementChannel { Wallet, Gmail }

data class MonthlyMovementBalance(val income: Long, val expense: Long) {
    val net: Long get() = income + expense
}

data class MovementsUiState(
    val query: String = "",
    val channel: MovementChannel? = null,
    val month: YearMonth = YearMonth.now(),
    val movements: List<HistoryMovement> = emptyList(),
    val reviewing: HistoryMovement? = null,
    val loading: Boolean = true,
    val error: Boolean = false,
    val showAll: Boolean = false,
    val historyLimit: Int = HISTORY_PAGE_SIZE,
    val referenceDate: LocalDate = LocalDate.now(),
) {
    val filtered: List<HistoryMovement>
        get() = movements.filter { movement ->
            YearMonth.from(movement.date) == month &&
                (channel == null || movement.channel == channel) &&
                (query.isBlank() || listOf(
                    movement.title, movement.detail, movement.source,
                    (movement.amountCents / 100.0).toString()
                ).any { it.contains(query.trim(), ignoreCase = true) })
        }.sortedWith(compareByDescending<HistoryMovement> { it.date }.thenByDescending { it.time })

    val recent: List<HistoryMovement>
        get() = filtered.filter {
            !it.date.isAfter(referenceDate)
        }.groupBy { it.date }.values.take(RECENT_TRANSACTION_DATES)
            .flatMap { it.take(RECENT_TRANSACTIONS_PER_DAY) }

    val visible: List<HistoryMovement>
        get() = if (showAll) filtered.take(historyLimit) else recent

    val hasMore: Boolean get() = showAll && historyLimit < filtered.size

    fun balance(channel: MovementChannel): MonthlyMovementBalance {
        val monthly = movements.filter {
            YearMonth.from(it.date) == month && it.channel == channel
        }
        return MonthlyMovementBalance(
            income = monthly.filter { it.amountCents > 0 }.sumOf { it.amountCents },
            expense = monthly.filter { it.amountCents < 0 }.sumOf { it.amountCents },
        )
    }

    companion object {
        const val RECENT_TRANSACTION_DATES = 3
        const val RECENT_TRANSACTIONS_PER_DAY = 3
        const val HISTORY_PAGE_SIZE = 20
    }
}

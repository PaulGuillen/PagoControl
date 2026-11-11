package com.devpaul.pagocontrol.features.movements.ui.movements

data class MovementsUiState(
    val query: String = "",
    val filter: String = "all",
    val source: String = "Todas",
    val status: String = "Todos",
    val month: String = "Noviembre 2023",
    val movements: List<HistoryMovement> = demoMovements(),
    val reviewing: HistoryMovement? = null,
    val notice: String? = null,
) {
    val visible: List<HistoryMovement>
        get() = movements.filter { movement ->
            month == "Noviembre 2023" &&
                (filter == "all" || movement.kind == filter || filter == "pending" && movement.pending) &&
                (source == "Todas" || movement.source == source) &&
                (status == "Todos" || if (status == "Pendientes") movement.pending else !movement.pending) &&
                (query.isBlank() || listOf(movement.title, movement.detail, movement.source,
                    (movement.amountCents / 100.0).toString()).any { it.contains(query.trim(), ignoreCase = true) })
        }
}

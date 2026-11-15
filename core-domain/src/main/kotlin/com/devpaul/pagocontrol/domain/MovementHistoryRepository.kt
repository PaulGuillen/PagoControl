package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

data class RecordedMovement(
    val id: String,
    val title: String,
    val source: String,
    val packageName: String,
    val amountCents: Long,
    val postedAt: Long,
    val notificationTitle: String,
    val notificationText: String,
)

interface MovementHistoryRepository {
    fun observeMovementHistory(): Flow<List<RecordedMovement>>
}

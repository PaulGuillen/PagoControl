package com.devpaul.pagocontrol.domain

fun interface MovementNotifier {
    suspend fun notify(movement: Movement)
}

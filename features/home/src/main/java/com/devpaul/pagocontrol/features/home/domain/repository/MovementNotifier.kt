package com.devpaul.pagocontrol.features.home.domain.repository

import com.devpaul.pagocontrol.features.home.domain.entity.Movement

fun interface MovementNotifier {
    suspend fun notify(movement: Movement)
}

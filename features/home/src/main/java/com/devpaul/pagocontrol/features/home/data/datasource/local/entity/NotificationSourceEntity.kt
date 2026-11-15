package com.devpaul.pagocontrol.features.home.data.datasource.local.entity

import androidx.room.Entity

@Entity(tableName = "notification_sources", primaryKeys = ["ownerId", "packageName"])
data class NotificationSourceEntity(val packageName: String, val ownerId: String = "")

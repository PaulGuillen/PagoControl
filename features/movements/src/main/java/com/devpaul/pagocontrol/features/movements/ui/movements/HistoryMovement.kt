package com.devpaul.pagocontrol.features.movements.ui.movements

import com.devpaul.pagocontrol.domain.RecordedMovement
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HistoryMovement(
    val id: String,
    val title: String,
    val detail: String,
    val source: String,
    val amountCents: Long,
    val channel: MovementChannel,
    val date: LocalDate,
    val time: LocalTime,
    val packageName: String,
    val notificationTitle: String,
    val notificationText: String,
)

internal fun RecordedMovement.toHistoryMovement(zone: ZoneId = ZoneId.systemDefault()): HistoryMovement {
    val dateTime = Instant.ofEpochMilli(postedAt).atZone(zone)
    return HistoryMovement(
        id = id,
        title = title,
        detail = dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
        source = source,
        amountCents = amountCents,
        channel = if (packageName == "com.google.android.gm") MovementChannel.Gmail else MovementChannel.Wallet,
        date = dateTime.toLocalDate(),
        time = dateTime.toLocalTime(),
        packageName = packageName,
        notificationTitle = notificationTitle,
        notificationText = notificationText,
    )
}

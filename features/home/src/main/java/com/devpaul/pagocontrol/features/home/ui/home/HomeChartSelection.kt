package com.devpaul.pagocontrol.features.home.ui.home

import java.util.Calendar

internal fun currentChartBar(monthly: Boolean, date: Calendar = Calendar.getInstance()): Int =
    if (monthly) (date.get(Calendar.DAY_OF_MONTH) - 1) / 7
    else (date.get(Calendar.DAY_OF_WEEK) + 5) % 7

internal fun currentChartDate(date: Calendar = Calendar.getInstance()): String =
    "${date.timeZone.id}:${date.get(Calendar.YEAR)}:${date.get(Calendar.DAY_OF_YEAR)}"

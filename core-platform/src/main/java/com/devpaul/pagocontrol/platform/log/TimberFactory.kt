package com.devpaul.pagocontrol.platform.log

import com.devpaul.pagocontrol.platform.BuildConfig
import timber.log.Timber

object TimberFactory {
    fun setup() {
        Timber.uprootAll()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}

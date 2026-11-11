package com.devpaul.core.platform.log

import com.devpaul.core.platform.BuildConfig
import timber.log.Timber

object TimberFactory {

    fun setup() {
        Timber.uprootAll()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

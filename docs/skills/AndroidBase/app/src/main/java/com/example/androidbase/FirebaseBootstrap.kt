package com.example.androidbase

import android.content.Context
import com.example.androidbase.platform.EventReporter
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber

object FirebaseBootstrap {
    fun initialize(context: Context) {
        checkNotNull(FirebaseApp.initializeApp(context)) { "Falta la configuración Firebase de esta variante" }
        AppCheckInstaller.install()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3_600)
                    .build(),
            )
            setDefaultsAsync(mapOf("example_enabled" to true))
            fetchAndActivate().addOnFailureListener { Timber.w("remote_config_fetch_failed") }
        }
    }
}

class DebugEventReporter : EventReporter {
    override fun screen(name: String) { Timber.d("screen:%s", name) }
    override fun action(name: String) { Timber.d("action:%s", name) }
    override fun error(operation: String) { Timber.w("operation_failed:%s", operation) }
}

class FirebaseEventReporter(context: Context) : EventReporter {
    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun screen(name: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, android.os.Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
        })
    }

    override fun action(name: String) {
        analytics.logEvent("ui_action", android.os.Bundle().apply { putString("action", name) })
    }

    override fun error(operation: String) {
        analytics.logEvent("operation_failed", android.os.Bundle().apply { putString("operation", operation) })
    }
}

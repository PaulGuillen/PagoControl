package com.devpaul.pagocontrol.platform.analytics

import android.content.Context
import android.os.Bundle
import com.devpaul.pagocontrol.platform.analytics.AnalyticsReporter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseAnalyticsReporter(context: Context, private val enabled: Boolean, environment: String) : AnalyticsReporter {
    private val analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    init {
        analytics.setAnalyticsCollectionEnabled(enabled)
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
        if (enabled) {
            analytics.setUserProperty("app_environment", environment)
            crashlytics.setCustomKey("app_environment", environment)
        }
    }

    override fun logEvent(name: String, parameters: Map<String, String>) {
        if (!enabled) {
            Timber.tag("PagoAnalytics").d("event=%s", name)
            return
        }
        val bundle = Bundle()
        parameters.forEach { (key, value) -> bundle.putString(key, value.take(100)) }
        analytics.logEvent(name, bundle)
        if (name == FirebaseAnalytics.Event.SCREEN_VIEW) crashlytics.setCustomKey("screen_name", parameters["screen_name"].orEmpty())
    }

    override fun recordError(operation: String, error: Throwable) {
        if (enabled) crashlytics.recordException(sanitizedAnalyticsError(operation, error))
    }
}

internal fun sanitizedAnalyticsError(operation: String, error: Throwable): Throwable =
    RuntimeException("$operation: ${error.javaClass.simpleName}").apply { stackTrace = error.stackTrace }

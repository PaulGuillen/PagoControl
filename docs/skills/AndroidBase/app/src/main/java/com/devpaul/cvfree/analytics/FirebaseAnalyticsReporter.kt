package com.devpaul.cvfree.analytics

import android.content.Context
import android.os.Bundle
import com.devpaul.core.platform.analytics.AnalyticsReporter
import com.devpaul.cvfree.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseAnalyticsReporter(context: Context) : AnalyticsReporter {
    private val analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    init {
        if (BuildConfig.DEBUG) {
            Timber.tag("CvAnalytics").d(
                "Initialized: project=%s, environment=%s",
                FirebaseApp.getInstance().options.projectId,
                BuildConfig.ENVIRONMENT,
            )
        }
        analytics.setUserProperty("app_environment", BuildConfig.ENVIRONMENT)
        analytics.setUserProperty("access_plan", BuildConfig.ACCESS_PLAN)
        crashlytics.setCustomKey("app_environment", BuildConfig.ENVIRONMENT)
        crashlytics.setCustomKey("access_plan", BuildConfig.ACCESS_PLAN)
    }

    override fun logEvent(name: String, parameters: Map<String, String>) {
        val bundle = Bundle()
        parameters.forEach { (key, value) -> bundle.putString(key, value.take(100)) }
        analytics.logEvent(name, bundle)
        if (BuildConfig.DEBUG) {
            Timber.tag("CvAnalytics").d("Passed to Firebase SDK: event=%s, parameters=%s", name, parameters)
        }
        if (name == FirebaseAnalytics.Event.SCREEN_VIEW) {
            crashlytics.setCustomKey("screen_name", parameters["screen_name"].orEmpty())
        }
    }

    override fun recordError(operation: String, error: Throwable) {
        crashlytics.recordException(sanitizedAnalyticsError(operation, error))
    }
}

internal fun sanitizedAnalyticsError(operation: String, error: Throwable): Throwable =
    ReportedAppException("$operation: ${error.javaClass.simpleName}").apply {
        stackTrace = error.stackTrace
    }

private class ReportedAppException(message: String) : RuntimeException(message)

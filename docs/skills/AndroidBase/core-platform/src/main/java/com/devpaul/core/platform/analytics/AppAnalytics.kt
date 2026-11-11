package com.devpaul.core.platform.analytics

import java.util.concurrent.CancellationException

interface AnalyticsReporter {
    fun logEvent(name: String, parameters: Map<String, String>)
    fun recordError(operation: String, error: Throwable)
}

object AppAnalytics {
    @Volatile
    private var reporter: AnalyticsReporter? = null

    @Volatile
    private var currentScreen = "app_start"

    fun initialize(reporter: AnalyticsReporter) {
        this.reporter = reporter
    }

    fun screenView(screen: String) {
        currentScreen = screen
        send("screen_view", mapOf("screen_name" to screen, "screen_class" to screen))
    }

    fun click(action: String) {
        send("ui_click", mapOf("action" to action))
    }

    fun selection(control: String, option: String) {
        send("ui_selection", mapOf("control" to control, "option" to option))
    }

    fun outcome(operation: String, result: String) {
        send("operation_result", mapOf("operation" to operation, "result" to result))
    }

    fun componentView(component: String) {
        send("component_view", mapOf("component" to component))
    }

    fun error(operation: String, error: Throwable? = null, code: String = "unknown") {
        if (error is CancellationException) return
        send("app_error", mapOf(
            "operation" to operation,
            "error_type" to (error?.javaClass?.simpleName ?: "HandledError"),
            "error_code" to code,
        ))
        if (error != null) {
            try {
                reporter?.recordError(operation, error)
            } catch (_: Exception) {
                return
            }
        }
    }

    private fun send(name: String, parameters: Map<String, String>) {
        try {
            reporter?.logEvent(name, mapOf("screen_name" to currentScreen) + parameters)
        } catch (_: Exception) {
            return
        }
    }
}

fun trackedClick(action: String, onClick: () -> Unit): () -> Unit = {
    AppAnalytics.click(action)
    onClick()
}

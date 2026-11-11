package com.devpaul.cvfree.analytics

import com.devpaul.core.platform.analytics.AnalyticsReporter
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.analytics.trackedClick
import java.util.concurrent.CancellationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppAnalyticsTest {
    private val reporter = RecordingReporter()

    @Before
    fun prepare() {
        AppAnalytics.initialize(reporter)
        AppAnalytics.screenView("home")
        reporter.events.clear()
    }

    @Test
    fun clickIsRecordedOnlyWhenCallbackRunsAndBeforeNavigation() {
        val callback = trackedClick("open_notifications") { AppAnalytics.screenView("notifications") }
        assertTrue(reporter.events.isEmpty())

        callback()

        assertEquals(listOf("ui_click", "screen_view"), reporter.events.map { it.first })
        assertEquals("home", reporter.events[0].second["screen_name"])
        assertEquals("open_notifications", reporter.events[0].second["action"])
        assertEquals("notifications", reporter.events[1].second["screen_name"])
    }

    @Test
    fun errorEventsExcludeExceptionMessagesAndCauses() {
        val error = IllegalArgumentException("email: private@example.com", RuntimeException("resume content"))

        AppAnalytics.error("import_resume", error)

        val event = reporter.events.single()
        assertEquals("app_error", event.first)
        assertEquals("IllegalArgumentException", event.second["error_type"])
        assertFalse(event.second.values.any { "private" in it || "resume content" in it })
        assertEquals(listOf("import_resume"), reporter.errors.map { it.first })
    }

    @Test
    fun cancellationIsNotReportedAsAnError() {
        AppAnalytics.error("import_resume", CancellationException())

        assertTrue(reporter.events.isEmpty())
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun validationErrorsAreEventsWithoutCrashReports() {
        AppAnalytics.error("login", code = "invalid_email")

        assertEquals("invalid_email", reporter.events.single().second["error_code"])
        assertTrue(reporter.errors.isEmpty())
    }

    @Test
    fun telemetryFailureDoesNotBlockUserActions() {
        AppAnalytics.initialize(object : AnalyticsReporter {
            override fun logEvent(name: String, parameters: Map<String, String>) = error("SDK unavailable")
            override fun recordError(operation: String, error: Throwable) = error("SDK unavailable")
        })
        var actionCompleted = false

        trackedClick("save_resume") { actionCompleted = true }()
        AppAnalytics.error("save_resume", IllegalStateException())

        assertTrue(actionCompleted)
    }

    @Test
    fun nonFatalCrashReportsKeepStackTraceWithoutPersonalContent() {
        val original = IllegalStateException("private@example.com", RuntimeException("CV details"))
        original.addSuppressed(RuntimeException("private file path"))

        val sanitized = sanitizedAnalyticsError("save_resume", original)

        assertEquals("save_resume: IllegalStateException", sanitized.message)
        assertArrayEquals(original.stackTrace, sanitized.stackTrace)
        assertNull(sanitized.cause)
        assertTrue(sanitized.suppressed.isEmpty())
    }

    @Test
    fun componentViewsPreserveTheCurrentScreenForFollowingActions() {
        AppAnalytics.componentView("language_dialog")
        AppAnalytics.selection("app_language", "english")
        AppAnalytics.outcome("save_resume", "success")

        assertEquals(listOf("component_view", "ui_selection", "operation_result"), reporter.events.map { it.first })
        assertTrue(reporter.events.all { it.second["screen_name"] == "home" })
    }

    private class RecordingReporter : AnalyticsReporter {
        val events = mutableListOf<Pair<String, Map<String, String>>>()
        val errors = mutableListOf<Pair<String, Throwable>>()

        override fun logEvent(name: String, parameters: Map<String, String>) {
            events += name to parameters
        }

        override fun recordError(operation: String, error: Throwable) {
            errors += operation to error
        }
    }
}

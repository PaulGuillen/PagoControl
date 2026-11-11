package com.devpaul.features.settings.ui

import com.devpaul.core.domain.rating.RATING_INTERVAL_MILLIS
import com.devpaul.core.domain.rating.RatingRepository
import com.devpaul.core.domain.rating.RatingResult
import com.devpaul.core.domain.rating.isRatingOnCooldown
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RatingViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeRatingRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cooldown permits first submission and expires exactly after 24 hours`() {
        assertFalse(isRatingOnCooldown(null, 0))
        assertTrue(isRatingOnCooldown(1000, 1000 + RATING_INTERVAL_MILLIS - 1))
        assertFalse(isRatingOnCooldown(1000, 1000 + RATING_INTERVAL_MILLIS))
        assertTrue(isRatingOnCooldown(1000, 999))
    }

    @Test
    fun `opening after a recent submission shows daily limit alert`() = runTest(dispatcher) {
        repository.availabilityResult = RatingResult.DailyLimitReached
        val viewModel = RatingViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        assertEquals(RatingDialogStatus.DailyLimitReached, viewModel.state.value.status)
        assertEquals(0, repository.submissionCount)
    }

    @Test
    fun `submission requires a star selection`() = runTest(dispatcher) {
        val viewModel = RatingViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(0, repository.submissionCount)
        assertEquals(RatingDialogStatus.Editing, viewModel.state.value.status)
    }

    @Test
    fun `double taps and dismiss while sending do not duplicate submission`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        repository.submissionGate = gate
        val viewModel = RatingViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.selectStars(5)
        viewModel.updateComment("Muy útil")
        viewModel.submit()
        viewModel.submit()
        viewModel.dismiss()
        runCurrent()
        assertEquals(RatingDialogStatus.Sending, viewModel.state.value.status)
        assertEquals(1, repository.submissionCount)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(RatingDialogStatus.Submitted, viewModel.state.value.status)
        assertEquals(5, repository.lastStars)
        assertEquals("Muy útil", repository.lastComment)
    }

    @Test
    fun `race with another device shows daily limit instead of success`() = runTest(dispatcher) {
        repository.submissionResult = RatingResult.DailyLimitReached
        val viewModel = RatingViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.selectStars(3)
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(RatingDialogStatus.DailyLimitReached, viewModel.state.value.status)
    }

    @Test
    fun `failed submission preserves feedback for retry`() = runTest(dispatcher) {
        repository.failSubmission = true
        val viewModel = RatingViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.selectStars(4)
        viewModel.updateComment("Me gusta")
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.hasError)
        assertEquals(RatingDialogStatus.Editing, viewModel.state.value.status)
        assertEquals(4, viewModel.state.value.stars)
        assertEquals("Me gusta", viewModel.state.value.comment)
        repository.failSubmission = false
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(RatingDialogStatus.Submitted, viewModel.state.value.status)
    }

    @Test
    fun `comment is limited to 500 characters`() = runTest(dispatcher) {
        val viewModel = RatingViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.updateComment("a".repeat(600))
        assertEquals(500, viewModel.state.value.comment.length)
    }

    private class FakeRatingRepository : RatingRepository {
        var availabilityResult = RatingResult.Available
        var submissionResult = RatingResult.Submitted
        var submissionCount = 0
        var lastStars = 0
        var lastComment = ""
        var failSubmission = false
        var submissionGate: CompletableDeferred<Unit>? = null

        override suspend fun availability() = availabilityResult

        override suspend fun submit(stars: Int, comment: String): RatingResult {
            submissionCount++
            lastStars = stars
            lastComment = comment
            submissionGate?.await()
            if (failSubmission) error("Network unavailable")
            return submissionResult
        }
    }
}

package com.devpaul.features.settings.ui

import com.devpaul.core.domain.rating.PremiumFeedbackRepository
import com.devpaul.core.domain.rating.RatingResult
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PremiumFeedbackViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakePremiumFeedbackRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `feedback requires stars and prevents duplicate submissions`() = runTest(dispatcher) {
        val viewModel = PremiumFeedbackViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(0, repository.submissions)

        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        viewModel.selectStars(4)
        viewModel.updateComment("More writing help")
        viewModel.submit()
        viewModel.submit()
        runCurrent()
        assertEquals(1, repository.submissions)
        assertEquals(RatingDialogStatus.Sending, viewModel.state.value.status)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(4, repository.lastStars)
        assertEquals("More writing help", repository.lastComment)
        assertEquals(RatingDialogStatus.Submitted, viewModel.state.value.status)
    }

    @Test
    fun `premium feedback observes its own daily limit`() = runTest(dispatcher) {
        repository.availabilityResult = RatingResult.DailyLimitReached
        val viewModel = PremiumFeedbackViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.selectStars(5)
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(RatingDialogStatus.DailyLimitReached, viewModel.state.value.status)
        assertEquals(0, repository.submissions)
    }

    @Test
    fun `failed write preserves stars and comment for retry`() = runTest(dispatcher) {
        val viewModel = PremiumFeedbackViewModel(repository)
        viewModel.open()
        advanceUntilIdle()
        viewModel.selectStars(3)
        viewModel.updateComment("Design options")
        repository.fail = true
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.hasError)
        assertEquals(3, viewModel.state.value.stars)
        assertEquals("Design options", viewModel.state.value.comment)
        repository.fail = false
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(RatingDialogStatus.Submitted, viewModel.state.value.status)
    }

    private class FakePremiumFeedbackRepository : PremiumFeedbackRepository {
        var availabilityResult = RatingResult.Available
        var gate: CompletableDeferred<Unit>? = null
        var fail = false
        var submissions = 0
        var lastStars = 0
        var lastComment = ""

        override suspend fun availability() = availabilityResult

        override suspend fun submit(stars: Int, comment: String): RatingResult {
            submissions++
            lastStars = stars
            lastComment = comment
            gate?.await()
            if (fail) error("Connection unavailable")
            return RatingResult.Submitted
        }
    }
}

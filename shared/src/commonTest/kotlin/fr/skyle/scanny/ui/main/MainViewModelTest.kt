package fr.skyle.scanny.ui.main

import fr.skyle.scanny.fake.FakePurgeHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the history is purged once at the start of the app`() = runTest(testDispatcher) {
        // Given
        val purgeHistoryUseCase = FakePurgeHistoryUseCase()

        // When
        MainViewModel(purgeHistoryUseCase = purgeHistoryUseCase)

        // Then
        assertEquals(
            expected = 1,
            actual = purgeHistoryUseCase.purgeCount,
        )
    }

    @Test
    fun `a purge that fails does not stop the app`() = runTest(testDispatcher) {
        // When
        MainViewModel(purgeHistoryUseCase = FakePurgeHistoryUseCase(error = IllegalStateException("Disk full")))

        // Then
        // No exception escapes the view model: the test would fail with it
    }
}
package fr.skyle.scanny.ui.screen.creationdetail.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.fake.FakeDeleteCreationUseCase
import fr.skyle.scanny.fake.FakeWatchCreationUseCase
import fr.skyle.scanny.fake.FixedClock
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class CreationDetailViewModelTest {

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
    fun `the creation is shown with its type its format its time and its content`() = runTest(testDispatcher) {
        // When
        val viewModel = creationDetailViewModel()

        // Then
        assertEquals(
            expected = CreationDetailItem(
                type = CodeType.LINK,
                format = BarcodeFormat.QR_CODE,
                createdDay = CalendarDay.Today,
                createdTime = "00:00",
                fields = listOf(
                    CodeField(
                        label = null,
                        value = "https://scanny.app",
                    ),
                ),
                rawValue = "https://scanny.app",
            ),
            actual = viewModel.uiState.value.creation,
        )
    }

    @Test
    fun `a creation that does not exist shows nothing`() = runTest(testDispatcher) {
        // When
        val viewModel = creationDetailViewModel(watchCreationUseCase = FakeWatchCreationUseCase(creation = null))

        // Then
        assertNull(viewModel.uiState.value.creation)
    }

    @Test
    fun `a deleted creation is gone and its screen closes`() = runTest(testDispatcher) {
        // Given
        val deleteCreationUseCase = FakeDeleteCreationUseCase()
        val viewModel = creationDetailViewModel(deleteCreationUseCase = deleteCreationUseCase)
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(CREATION_ID),
            actual = deleteCreationUseCase.deletedCreationIds,
        )
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.CreationDeleted),
            actual = events,
        )
    }

    @Test
    fun `a creation that cannot be deleted is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = creationDetailViewModel(deleteCreationUseCase = FakeDeleteCreationUseCase(error = IllegalStateException("Disk full")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.DeleteFailed),
            actual = events,
        )
    }

    private fun creationDetailViewModel(
        watchCreationUseCase: FakeWatchCreationUseCase = FakeWatchCreationUseCase(creation = linkCreation()),
        deleteCreationUseCase: FakeDeleteCreationUseCase = FakeDeleteCreationUseCase(),
    ): CreationDetailViewModel =
        CreationDetailViewModel(
            creationId = CREATION_ID,
            watchCreationUseCase = watchCreationUseCase,
            deleteCreationUseCase = deleteCreationUseCase,
            // Created at the very moment of the test, in UTC
            clock = FixedClock(instant = Instant.fromEpochMilliseconds(0)),
            timeZone = TimeZone.UTC,
        )

    private fun linkCreation(): Creation =
        Creation(
            id = CREATION_ID,
            content = BarcodeContent.Url(url = "https://scanny.app"),
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            createdAt = Instant.fromEpochMilliseconds(0),
        )

    private fun TestScope.collectEvents(viewModel: CreationDetailViewModel): List<CreationDetailViewModel.CreationDetailEvent> {
        val events = mutableListOf<CreationDetailViewModel.CreationDetailEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private companion object {
        const val CREATION_ID = 3L
    }
}
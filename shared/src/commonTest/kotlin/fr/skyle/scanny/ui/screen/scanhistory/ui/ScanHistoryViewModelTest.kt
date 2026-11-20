package fr.skyle.scanny.ui.screen.scanhistory.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.fake.FakeDeleteAllScansUseCase
import fr.skyle.scanny.fake.FakeWatchScansUseCase
import fr.skyle.scanny.fake.FixedClock
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.scanhistory.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.scanhistory.ui.model.ScanHistorySection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class ScanHistoryViewModelTest {

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
    fun `the captures are shown by day from the newest`() = runTest(testDispatcher) {
        // Given
        val watchScansUseCase = FakeWatchScansUseCase(
            scans = listOf(
                scan(
                    id = 4,
                    scannedAt = "2026-11-20T21:48:00Z",
                    codes = listOf(
                        code(
                            id = 5,
                            rawValue = "https://scanny.app",
                            content = BarcodeContent.Url(url = "https://scanny.app"),
                        ),
                    ),
                ),
                scan(
                    id = 3,
                    scannedAt = "2026-11-20T09:05:00Z",
                    codes = listOf(
                        code(
                            id = 3,
                            rawValue = "First",
                        ),
                        code(
                            id = 4,
                            rawValue = "Second",
                        ),
                    ),
                ),
                scan(
                    id = 2,
                    scannedAt = "2026-11-19T18:00:00Z",
                    codes = listOf(
                        code(
                            id = 2,
                            rawValue = "Yesterday",
                        ),
                    ),
                ),
                scan(
                    id = 1,
                    scannedAt = "2026-11-02T08:30:00Z",
                    codes = listOf(
                        code(
                            id = 1,
                            rawValue = "Before",
                        ),
                    ),
                ),
            ),
        )

        // When
        val viewModel = scanHistoryViewModel(watchScansUseCase = watchScansUseCase)

        // Then
        assertEquals(
            expected = listOf(
                ScanHistorySection(
                    day = CalendarDay.Today,
                    items = listOf(
                        ScanHistoryItem(
                            scanId = 4,
                            firstCodeId = 5,
                            type = CodeType.LINK,
                            summary = "https://scanny.app",
                            time = "21:48",
                            codeCount = 1,
                        ),
                        ScanHistoryItem(
                            scanId = 3,
                            firstCodeId = 3,
                            type = CodeType.TEXT,
                            summary = "First",
                            time = "09:05",
                            codeCount = 2,
                        ),
                    ),
                ),
                ScanHistorySection(
                    day = CalendarDay.Yesterday,
                    items = listOf(
                        item(
                            scanId = 2,
                            summary = "Yesterday",
                            time = "18:00",
                        ),
                    ),
                ),
                ScanHistorySection(
                    day = CalendarDay.Date(
                        date = LocalDate(
                            year = 2026,
                            month = 11,
                            day = 2,
                        ),
                    ),
                    items = listOf(
                        item(
                            scanId = 1,
                            summary = "Before",
                            time = "08:30",
                        ),
                    ),
                ),
            ),
            actual = viewModel.uiState.value.sections,
        )
    }

    @Test
    fun `an empty history has no section`() = runTest(testDispatcher) {
        // When
        val viewModel = scanHistoryViewModel(watchScansUseCase = FakeWatchScansUseCase(scans = emptyList()))

        // Then
        assertEquals(
            expected = emptyList(),
            actual = viewModel.uiState.value.sections,
        )
    }

    @Test
    fun `the history follows the captures recorded and deleted`() = runTest(testDispatcher) {
        // Given
        val watchScansUseCase = FakeWatchScansUseCase(scans = emptyList())
        val viewModel = scanHistoryViewModel(watchScansUseCase = watchScansUseCase)

        // When
        watchScansUseCase.scans.value = listOf(
            scan(
                id = 1,
                scannedAt = "2026-11-20T10:00:00Z",
                codes = listOf(
                    code(
                        id = 1,
                        rawValue = "New",
                    ),
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(1L),
            actual = viewModel.uiState.value.sections?.flatMap { section ->
                section.items.map { it.scanId }
            },
        )
    }

    @Test
    fun `clearing the history deletes every capture`() = runTest(testDispatcher) {
        // Given
        val deleteAllScansUseCase = FakeDeleteAllScansUseCase()
        val viewModel = scanHistoryViewModel(deleteAllScansUseCase = deleteAllScansUseCase)

        // When
        viewModel.onClearConfirmed()

        // Then
        assertEquals(
            expected = 1,
            actual = deleteAllScansUseCase.clearCount,
        )
    }

    @Test
    fun `a history that cannot be cleared is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanHistoryViewModel(
            deleteAllScansUseCase = FakeDeleteAllScansUseCase(error = IllegalStateException("Disk full")),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onClearConfirmed()

        // Then
        assertEquals(
            expected = listOf(ScanHistoryViewModel.ScanHistoryEvent.ClearFailed),
            actual = events,
        )
    }

    private fun scanHistoryViewModel(
        watchScansUseCase: FakeWatchScansUseCase = FakeWatchScansUseCase(),
        deleteAllScansUseCase: FakeDeleteAllScansUseCase = FakeDeleteAllScansUseCase(),
    ): ScanHistoryViewModel =
        ScanHistoryViewModel(
            watchScansUseCase = watchScansUseCase,
            deleteAllScansUseCase = deleteAllScansUseCase,
            clock = FixedClock(instant = Instant.parse("2026-11-20T23:00:00Z")),
            timeZone = TimeZone.UTC,
        )

    private fun TestScope.collectEvents(viewModel: ScanHistoryViewModel): List<ScanHistoryViewModel.ScanHistoryEvent> {
        val events = mutableListOf<ScanHistoryViewModel.ScanHistoryEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private fun scan(
        id: Long,
        scannedAt: String,
        codes: List<ScannedCode>,
    ): Scan =
        Scan(
            id = id,
            scannedAt = Instant.parse(scannedAt),
            codes = codes.map { code ->
                code.copy(scanId = id)
            },
        )

    private fun code(
        id: Long,
        rawValue: String,
        content: BarcodeContent = BarcodeContent.Text(text = rawValue),
    ): ScannedCode =
        ScannedCode(
            id = id,
            scanId = 0,
            rawValue = rawValue,
            content = content,
            format = BarcodeFormat.QR_CODE,
            scannedAt = Instant.fromEpochMilliseconds(0),
        )

    private fun item(
        scanId: Long,
        summary: String,
        time: String,
    ): ScanHistoryItem =
        ScanHistoryItem(
            scanId = scanId,
            firstCodeId = scanId,
            type = CodeType.TEXT,
            summary = summary,
            time = time,
            codeCount = 1,
        )
}
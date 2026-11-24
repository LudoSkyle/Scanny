package fr.skyle.scanny.ui.screen.history.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.payload.toRawValue
import fr.skyle.scanny.fake.FakeDeleteAllCreationsUseCase
import fr.skyle.scanny.fake.FakeDeleteAllScansUseCase
import fr.skyle.scanny.fake.FakeWatchCreationsUseCase
import fr.skyle.scanny.fake.FakeWatchScansUseCase
import fr.skyle.scanny.fake.FixedClock
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryHeader
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryView
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistorySection
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
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

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

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
                            rawValue = "https://scanny.app/second",
                            content = BarcodeContent.Url(url = "https://scanny.app/second"),
                        ),
                        code(
                            id = 6,
                            rawValue = "Third",
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
        val viewModel = historyViewModel(watchScansUseCase = watchScansUseCase)

        // Then
        assertEquals(
            expected = listOf(
                ScanHistorySection(
                    header = HistoryHeader.Day(day = CalendarDay.Today),
                    items = listOf(
                        ScanHistoryItem(
                            scanId = 4,
                            firstCodeId = 5,
                            type = CodeType.LINK,
                            format = BarcodeFormat.QR_CODE,
                            summary = "https://scanny.app",
                            time = "21:48",
                            codeCount = 1,
                            codeTypes = listOf(CodeType.LINK),
                        ),
                        ScanHistoryItem(
                            scanId = 3,
                            firstCodeId = 3,
                            type = CodeType.TEXT,
                            format = BarcodeFormat.QR_CODE,
                            summary = "First",
                            time = "09:05",
                            codeCount = 3,
                            // Each type once, in the order of the codes
                            codeTypes = listOf(CodeType.TEXT, CodeType.LINK),
                        ),
                    ),
                ),
                ScanHistorySection(
                    header = HistoryHeader.Day(day = CalendarDay.Yesterday),
                    items = listOf(
                        item(
                            scanId = 2,
                            summary = "Yesterday",
                            time = "18:00",
                        ),
                    ),
                ),
                ScanHistorySection(
                    header = HistoryHeader.Day(day = CalendarDay.Date(
                        date = LocalDate(
                            year = 2026,
                            month = 11,
                            day = 2,
                        ),
                    )),
                    items = listOf(
                        item(
                            scanId = 1,
                            summary = "Before",
                            time = "08:30",
                        ),
                    ),
                ),
            ),
            actual = viewModel.uiState.value.scanSections,
        )
    }

    @Test
    fun `the favorite captures come first apart and leave their day`() = runTest(testDispatcher) {
        // Given
        val watchScansUseCase = FakeWatchScansUseCase(
            scans = listOf(
                scan(
                    id = 3,
                    scannedAt = "2026-11-20T21:48:00Z",
                    codes = listOf(code(id = 3, rawValue = "Today")),
                ),
                scan(
                    id = 2,
                    scannedAt = "2026-11-20T09:05:00Z",
                    codes = listOf(code(id = 2, rawValue = "Favorite of today")),
                    isFavorite = true,
                ),
                scan(
                    id = 1,
                    scannedAt = "2026-11-02T08:30:00Z",
                    codes = listOf(code(id = 1, rawValue = "Older favorite")),
                    isFavorite = true,
                ),
            ),
        )

        // When
        val viewModel = historyViewModel(watchScansUseCase = watchScansUseCase)

        // Then
        assertEquals(
            expected = listOf(
                HistoryHeader.Favorites to listOf(2L, 1L),
                HistoryHeader.Day(day = CalendarDay.Today) to listOf(3L),
            ),
            actual = viewModel.uiState.value.scanSections?.map { section ->
                section.header to section.items.map { item -> item.scanId }
            },
        )
    }

    @Test
    fun `the favorite creations come first apart and leave their day`() = runTest(testDispatcher) {
        // Given
        val watchCreationsUseCase = FakeWatchCreationsUseCase(
            creations = listOf(
                creation(
                    id = 2,
                    createdAt = "2026-11-20T21:48:00Z",
                    content = BarcodeContent.Text(text = "Today"),
                ),
                creation(
                    id = 1,
                    createdAt = "2026-11-02T08:30:00Z",
                    content = BarcodeContent.Text(text = "Older favorite"),
                    isFavorite = true,
                ),
            ),
        )

        // When
        val viewModel = historyViewModel(watchCreationsUseCase = watchCreationsUseCase)

        // Then
        assertEquals(
            expected = listOf(
                HistoryHeader.Favorites to listOf(1L),
                HistoryHeader.Day(day = CalendarDay.Today) to listOf(2L),
            ),
            actual = viewModel.uiState.value.creationSections?.map { section ->
                section.header to section.items.map { item -> item.creationId }
            },
        )
    }

    @Test
    fun `an empty history has no section`() = runTest(testDispatcher) {
        // When
        val viewModel = historyViewModel(watchScansUseCase = FakeWatchScansUseCase(scans = emptyList()))

        // Then
        assertEquals(
            expected = emptyList(),
            actual = viewModel.uiState.value.scanSections,
        )
    }

    @Test
    fun `the history follows the captures recorded and deleted`() = runTest(testDispatcher) {
        // Given
        val watchScansUseCase = FakeWatchScansUseCase(scans = emptyList())
        val viewModel = historyViewModel(watchScansUseCase = watchScansUseCase)

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
            actual = viewModel.uiState.value.scanSections?.flatMap { section ->
                section.items.map { it.scanId }
            },
        )
    }

    @Test
    fun `a capture of one code opens its detail and a capture of several its group`() = runTest(testDispatcher) {
        // Given
        val viewModel = historyViewModel()
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onScanClicked(
            item = item(
                scanId = 1,
                summary = "Alone",
                time = "10:00",
            ),
        )
        viewModel.onScanClicked(
            item = item(
                scanId = 2,
                summary = "First of three",
                time = "11:00",
            ).copy(
                firstCodeId = 7,
                codeCount = 3,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                HistoryViewModel.HistoryEvent.OpenScanDetail(codeId = 1),
                HistoryViewModel.HistoryEvent.OpenScanGroup(scanId = 2),
            ),
            actual = events,
        )
    }

    @Test
    fun `clearing the scans deletes every capture and keeps the creations`() = runTest(testDispatcher) {
        // Given
        val deleteAllScansUseCase = FakeDeleteAllScansUseCase()
        val deleteAllCreationsUseCase = FakeDeleteAllCreationsUseCase()
        val viewModel = historyViewModel(
            deleteAllScansUseCase = deleteAllScansUseCase,
            deleteAllCreationsUseCase = deleteAllCreationsUseCase,
        )

        // When
        viewModel.onClearConfirmed()

        // Then
        assertEquals(
            expected = 1 to 0,
            actual = deleteAllScansUseCase.clearCount to deleteAllCreationsUseCase.clearCount,
        )
    }

    @Test
    fun `a history that cannot be cleared is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = historyViewModel(
            deleteAllScansUseCase = FakeDeleteAllScansUseCase(error = IllegalStateException("Disk full")),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onClearConfirmed()

        // Then
        assertEquals(
            expected = listOf(HistoryViewModel.HistoryEvent.ClearFailed),
            actual = events,
        )
    }

    @Test
    fun `the history opens on the scans`() = runTest(testDispatcher) {
        // When
        val viewModel = historyViewModel()

        // Then
        assertEquals(
            expected = HistoryView.SCANS,
            actual = viewModel.uiState.value.selectedView,
        )
    }

    @Test
    fun `the creations are shown by day from the newest`() = runTest(testDispatcher) {
        // Given
        val watchCreationsUseCase = FakeWatchCreationsUseCase(
            creations = listOf(
                creation(
                    id = 2,
                    createdAt = "2026-11-20T21:48:00Z",
                    content = BarcodeContent.Wifi(
                        ssid = "Scanny",
                        encryption = WifiEncryption.WPA,
                        password = "secret12",
                    ),
                ),
                creation(
                    id = 1,
                    createdAt = "2026-11-19T18:00:00Z",
                    content = BarcodeContent.Text(text = "Yesterday"),
                ),
            ),
        )

        // When
        val viewModel = historyViewModel(watchCreationsUseCase = watchCreationsUseCase)
        viewModel.onViewSelected(view = HistoryView.CREATIONS)

        // Then
        assertEquals(
            expected = HistoryView.CREATIONS to listOf(
                CreationHistorySection(
                    header = HistoryHeader.Day(day = CalendarDay.Today),
                    items = listOf(
                        CreationHistoryItem(
                            creationId = 2,
                            type = CodeType.WIFI,
                            format = BarcodeFormat.QR_CODE,
                            summary = "Scanny",
                            time = "21:48",
                        ),
                    ),
                ),
                CreationHistorySection(
                    header = HistoryHeader.Day(day = CalendarDay.Yesterday),
                    items = listOf(
                        CreationHistoryItem(
                            creationId = 1,
                            type = CodeType.TEXT,
                            format = BarcodeFormat.QR_CODE,
                            summary = "Yesterday",
                            time = "18:00",
                        ),
                    ),
                ),
            ),
            actual = viewModel.uiState.value.selectedView to viewModel.uiState.value.creationSections,
        )
    }

    @Test
    fun `a creation opens its detail`() = runTest(testDispatcher) {
        // Given
        val viewModel = historyViewModel()
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onCreationClicked(
            item = CreationHistoryItem(
                creationId = 7,
                type = CodeType.TEXT,
                format = BarcodeFormat.QR_CODE,
                summary = "Created",
                time = "10:00",
            ),
        )

        // Then
        assertEquals(
            expected = listOf(HistoryViewModel.HistoryEvent.OpenCreationDetail(creationId = 7)),
            actual = events,
        )
    }

    @Test
    fun `clearing the creations keeps the scans`() = runTest(testDispatcher) {
        // Given
        val deleteAllScansUseCase = FakeDeleteAllScansUseCase()
        val deleteAllCreationsUseCase = FakeDeleteAllCreationsUseCase()
        val viewModel = historyViewModel(
            deleteAllScansUseCase = deleteAllScansUseCase,
            deleteAllCreationsUseCase = deleteAllCreationsUseCase,
        )
        viewModel.onViewSelected(view = HistoryView.CREATIONS)

        // When
        viewModel.onClearConfirmed()

        // Then
        assertEquals(
            expected = 0 to 1,
            actual = deleteAllScansUseCase.clearCount to deleteAllCreationsUseCase.clearCount,
        )
    }

    private fun historyViewModel(
        watchScansUseCase: FakeWatchScansUseCase = FakeWatchScansUseCase(),
        watchCreationsUseCase: FakeWatchCreationsUseCase = FakeWatchCreationsUseCase(),
        deleteAllScansUseCase: FakeDeleteAllScansUseCase = FakeDeleteAllScansUseCase(),
        deleteAllCreationsUseCase: FakeDeleteAllCreationsUseCase = FakeDeleteAllCreationsUseCase(),
    ): HistoryViewModel =
        HistoryViewModel(
            watchScansUseCase = watchScansUseCase,
            watchCreationsUseCase = watchCreationsUseCase,
            deleteAllScansUseCase = deleteAllScansUseCase,
            deleteAllCreationsUseCase = deleteAllCreationsUseCase,
            clock = FixedClock(instant = Instant.parse("2026-11-20T23:00:00Z")),
            timeZone = TimeZone.UTC,
        )

    private fun TestScope.collectEvents(viewModel: HistoryViewModel): List<HistoryViewModel.HistoryEvent> {
        val events = mutableListOf<HistoryViewModel.HistoryEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private fun scan(
        id: Long,
        scannedAt: String,
        codes: List<ScannedCode>,
        isFavorite: Boolean = false,
    ): Scan =
        Scan(
            id = id,
            scannedAt = Instant.parse(scannedAt),
            codes = codes.map { code ->
                code.copy(scanId = id)
            },
            isFavorite = isFavorite,
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
            isFavorite = false,
            isInGroup = false,
        )

    private fun creation(
        id: Long,
        createdAt: String,
        content: BarcodeContent,
        isFavorite: Boolean = false,
    ): Creation =
        Creation(
            id = id,
            content = content,
            rawValue = content.toRawValue(),
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(),
            createdAt = Instant.parse(createdAt),
            isFavorite = isFavorite,
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
            format = BarcodeFormat.QR_CODE,
            summary = summary,
            time = time,
            codeCount = 1,
            codeTypes = listOf(CodeType.TEXT),
        )
}
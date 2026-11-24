package fr.skyle.scanny.ui.screen.scangroup.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.fake.FakeDeleteScanUseCase
import fr.skyle.scanny.fake.FakeSetScanFavoriteUseCase
import fr.skyle.scanny.fake.FakeTextSharer
import fr.skyle.scanny.fake.FakeWatchScanUseCase
import fr.skyle.scanny.fake.FixedClock
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.scangroup.ui.model.ScanGroupItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
import kotlinx.datetime.TimeZone

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class ScanGroupViewModelTest {

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
    fun `the group of the screen is shown with its codes and its time`() = runTest(testDispatcher) {
        // Given
        val watchScanUseCase = FakeWatchScanUseCase(scan = group())

        // When
        val viewModel = scanGroupViewModel(watchScanUseCase = watchScanUseCase)

        // Then
        assertEquals(
            expected = listOf(SCAN_ID),
            actual = watchScanUseCase.watchedScanIds,
        )
        assertEquals(
            expected = ScanGroupItem(
                scannedDay = CalendarDay.Today,
                scannedTime = "21:48",
                codes = listOf(
                    CodeRowItem(
                        codeId = 1,
                        type = CodeType.LINK,
                        summary = "https://scanny.app",
                        format = BarcodeFormat.QR_CODE,
                    ),
                    CodeRowItem(
                        codeId = 2,
                        type = CodeType.TEXT,
                        summary = "4006381333931",
                        format = BarcodeFormat.QR_CODE,
                    ),
                ),
                isFavorite = false,
            ),
            actual = viewModel.uiState.value.group,
        )
    }

    @Test
    fun `sharing the group shares the raw value of each code on its own line`() = runTest(testDispatcher) {
        // Given
        val textSharer = FakeTextSharer()
        val viewModel = scanGroupViewModel(textSharer = textSharer)

        // When
        viewModel.onShareClicked()

        // Then
        assertEquals(
            expected = listOf("https://scanny.app\n4006381333931"),
            actual = textSharer.sharedTexts,
        )
    }

    @Test
    fun `deleting the group deletes its capture`() = runTest(testDispatcher) {
        // Given
        val deleteScanUseCase = FakeDeleteScanUseCase()
        val viewModel = scanGroupViewModel(deleteScanUseCase = deleteScanUseCase)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(SCAN_ID),
            actual = deleteScanUseCase.deletedScanIds,
        )
    }

    @Test
    fun `a group that is no longer there closes the screen`() = runTest(testDispatcher) {
        // Given
        val watchScanUseCase = FakeWatchScanUseCase(scan = group())
        val viewModel = scanGroupViewModel(watchScanUseCase = watchScanUseCase)
        val events = collectEvents(viewModel = viewModel)

        // When
        watchScanUseCase.scan.value = null

        // Then
        assertEquals(
            expected = listOf(ScanGroupViewModel.ScanGroupEvent.GroupGone),
            actual = events,
        )
    }

    @Test
    fun `a group that does not exist shows nothing and stays`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanGroupViewModel(watchScanUseCase = FakeWatchScanUseCase(scan = null))

        // When
        val events = collectEvents(viewModel = viewModel)

        // Then
        assertEquals(
            expected = emptyList(),
            actual = events,
        )
        assertNull(viewModel.uiState.value.group)
    }

    @Test
    fun `a group that cannot be deleted is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanGroupViewModel(deleteScanUseCase = FakeDeleteScanUseCase(error = IllegalStateException("Disk full")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(ScanGroupViewModel.ScanGroupEvent.DeleteFailed),
            actual = events,
        )
    }

    @Test
    fun `the star puts the group among the favorites as a whole and takes it out`() = runTest(testDispatcher) {
        // Given
        val setScanFavoriteUseCase = FakeSetScanFavoriteUseCase()
        val notFavorite = scanGroupViewModel(setScanFavoriteUseCase = setScanFavoriteUseCase)
        val favorite = scanGroupViewModel(
            watchScanUseCase = FakeWatchScanUseCase(scan = group().copy(isFavorite = true)),
            setScanFavoriteUseCase = setScanFavoriteUseCase,
        )

        // When
        notFavorite.onFavoriteClicked()
        favorite.onFavoriteClicked()

        // Then
        assertEquals(
            expected = listOf(SCAN_ID to true, SCAN_ID to false),
            actual = setScanFavoriteUseCase.setFavorites,
        )
    }

    private fun scanGroupViewModel(
        watchScanUseCase: FakeWatchScanUseCase = FakeWatchScanUseCase(scan = group()),
        deleteScanUseCase: FakeDeleteScanUseCase = FakeDeleteScanUseCase(),
        setScanFavoriteUseCase: FakeSetScanFavoriteUseCase = FakeSetScanFavoriteUseCase(),
        textSharer: FakeTextSharer = FakeTextSharer(),
    ): ScanGroupViewModel =
        ScanGroupViewModel(
            scanId = SCAN_ID,
            watchScanUseCase = watchScanUseCase,
            deleteScanUseCase = deleteScanUseCase,
            setScanFavoriteUseCase = setScanFavoriteUseCase,
            textSharer = textSharer,
            clock = FixedClock(instant = SCANNED_AT),
            timeZone = TimeZone.UTC,
        )

    private fun TestScope.collectEvents(viewModel: ScanGroupViewModel): List<ScanGroupViewModel.ScanGroupEvent> {
        val events = mutableListOf<ScanGroupViewModel.ScanGroupEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private fun group(): Scan =
        Scan(
            id = SCAN_ID,
            scannedAt = SCANNED_AT,
            codes = listOf(
                code(
                    id = 1,
                    rawValue = "https://scanny.app",
                    content = BarcodeContent.Url(url = "https://scanny.app"),
                ),
                code(
                    id = 2,
                    rawValue = "4006381333931",
                    content = BarcodeContent.Text(text = "4006381333931"),
                ),
            ),
            isFavorite = false,
        )

    private fun code(
        id: Long,
        rawValue: String,
        content: BarcodeContent,
    ): ScannedCode =
        ScannedCode(
            id = id,
            scanId = SCAN_ID,
            rawValue = rawValue,
            content = content,
            format = BarcodeFormat.QR_CODE,
            scannedAt = SCANNED_AT,
            isFavorite = false,
            isInGroup = false,
        )

    private companion object {
        const val SCAN_ID = 5L
        val SCANNED_AT = Instant.parse("2026-11-20T21:48:00Z")
    }
}
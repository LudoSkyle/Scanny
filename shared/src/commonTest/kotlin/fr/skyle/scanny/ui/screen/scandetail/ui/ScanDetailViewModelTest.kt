package fr.skyle.scanny.ui.screen.scandetail.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.fake.FakeContactAdder
import fr.skyle.scanny.fake.FakeDeleteScannedCodeUseCase
import fr.skyle.scanny.fake.FakeTextSharer
import fr.skyle.scanny.fake.FakeWatchScanSettingsUseCase
import fr.skyle.scanny.fake.FakeWatchScannedCodeUseCase
import fr.skyle.scanny.fake.FakeWifiNetworkSupport
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class ScanDetailViewModelTest {

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
    fun `the code of the screen is shown with its content`() = runTest(testDispatcher) {
        // Given
        val watchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = wifiCode())

        // When
        val viewModel = scanDetailViewModel(watchScannedCodeUseCase = watchScannedCodeUseCase)

        // Then
        assertEquals(
            expected = listOf(CODE_ID),
            actual = watchScannedCodeUseCase.watchedCodeIds,
        )
        assertEquals(
            expected = ScanDetailItem(
                type = CodeType.WIFI,
                format = BarcodeFormat.QR_CODE,
                fields = listOf(
                    CodeField(
                        label = CodeFieldLabel.NETWORK,
                        value = "Scanny",
                    ),
                    CodeField(
                        label = CodeFieldLabel.SECURITY,
                        value = "WPA",
                    ),
                    CodeField(
                        label = CodeFieldLabel.PASSWORD,
                        value = "secret",
                    ),
                ),
                rawValue = WIFI_RAW_VALUE,
                isRawValueShown = false,
                action = null,
            ),
            actual = viewModel.uiState.value.code,
        )
    }

    @Test
    fun `a network the device can join is shown with the action to join it`() = runTest(testDispatcher) {
        // When
        val viewModel = scanDetailViewModel(wifiNetworkSupport = FakeWifiNetworkSupport(canJoin = true))

        // Then
        assertEquals(
            expected = CodeAction.JoinWifi(wifi = WIFI),
            actual = viewModel.uiState.value.code?.action,
        )
    }

    @Test
    fun `adding the contact opens the contacts`() = runTest(testDispatcher) {
        // Given
        val contactAdder = FakeContactAdder()
        val viewModel = scanDetailViewModel(contactAdder = contactAdder)
        val contact = BarcodeContent.Contact(formattedName = "John Doe")

        // When
        viewModel.onAddContactClicked(contact = contact)

        // Then
        assertEquals(
            expected = listOf(contact),
            actual = contactAdder.addedContacts,
        )
    }

    @Test
    fun `a contact that cannot be added is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanDetailViewModel(contactAdder = FakeContactAdder(error = IllegalStateException("No contacts app")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onAddContactClicked(contact = BarcodeContent.Contact(formattedName = "John Doe"))

        // Then
        assertEquals(
            expected = listOf(ScanDetailViewModel.ScanDetailEvent.ContactNotAdded),
            actual = events,
        )
    }

    @Test
    fun `the raw content follows its setting`() = runTest(testDispatcher) {
        // Given
        val watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isRawContentShown = false)
        val viewModel = scanDetailViewModel(watchScanSettingsUseCase = watchScanSettingsUseCase)

        // When
        watchScanSettingsUseCase.settings.update { settings ->
            settings.copy(isRawContentShown = true)
        }

        // Then
        assertEquals(
            expected = true,
            actual = viewModel.uiState.value.code?.isRawValueShown,
        )
    }

    @Test
    fun `the raw content of a link is not shown twice`() = runTest(testDispatcher) {
        // Given
        val link = scannedCode(
            rawValue = "https://scanny.app",
            content = BarcodeContent.Url(url = "https://scanny.app"),
        )

        // When
        val viewModel = scanDetailViewModel(
            watchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = link),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isRawContentShown = true),
        )

        // Then
        assertEquals(
            expected = false,
            actual = viewModel.uiState.value.code?.isRawValueShown,
        )
    }

    @Test
    fun `a code that does not exist shows nothing`() = runTest(testDispatcher) {
        // When
        val viewModel = scanDetailViewModel(watchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = null))

        // Then
        assertNull(viewModel.uiState.value.code)
    }

    @Test
    fun `sharing the code shares its raw value`() = runTest(testDispatcher) {
        // Given
        val textSharer = FakeTextSharer()
        val viewModel = scanDetailViewModel(textSharer = textSharer)

        // When
        viewModel.onShareClicked()

        // Then
        assertEquals(
            expected = listOf(WIFI_RAW_VALUE),
            actual = textSharer.sharedTexts,
        )
    }

    @Test
    fun `a deleted code closes the screen`() = runTest(testDispatcher) {
        // Given
        val deleteScannedCodeUseCase = FakeDeleteScannedCodeUseCase()
        val viewModel = scanDetailViewModel(deleteScannedCodeUseCase = deleteScannedCodeUseCase)
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(CODE_ID),
            actual = deleteScannedCodeUseCase.deletedCodeIds,
        )
        assertEquals(
            expected = listOf(ScanDetailViewModel.ScanDetailEvent.CodeDeleted),
            actual = events,
        )
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `a code that cannot be deleted is reported and stays`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanDetailViewModel(
            deleteScannedCodeUseCase = FakeDeleteScannedCodeUseCase(error = IllegalStateException("Disk full")),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(ScanDetailViewModel.ScanDetailEvent.DeleteFailed),
            actual = events,
        )
        assertFalse(viewModel.uiState.value.isDeleting)
        assertNotNull(viewModel.uiState.value.code)
    }

    private fun scanDetailViewModel(
        watchScannedCodeUseCase: FakeWatchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = wifiCode()),
        watchScanSettingsUseCase: FakeWatchScanSettingsUseCase = FakeWatchScanSettingsUseCase(),
        deleteScannedCodeUseCase: FakeDeleteScannedCodeUseCase = FakeDeleteScannedCodeUseCase(),
        textSharer: FakeTextSharer = FakeTextSharer(),
        contactAdder: FakeContactAdder = FakeContactAdder(),
        wifiNetworkSupport: FakeWifiNetworkSupport = FakeWifiNetworkSupport(),
    ): ScanDetailViewModel =
        ScanDetailViewModel(
            codeId = CODE_ID,
            watchScannedCodeUseCase = watchScannedCodeUseCase,
            watchScanSettingsUseCase = watchScanSettingsUseCase,
            deleteScannedCodeUseCase = deleteScannedCodeUseCase,
            textSharer = textSharer,
            contactAdder = contactAdder,
            wifiNetworkSupport = wifiNetworkSupport,
        )

    private fun TestScope.collectEvents(viewModel: ScanDetailViewModel): List<ScanDetailViewModel.ScanDetailEvent> {
        val events = mutableListOf<ScanDetailViewModel.ScanDetailEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private fun wifiCode(): ScannedCode =
        scannedCode(
            rawValue = WIFI_RAW_VALUE,
            content = WIFI,
        )

    private fun scannedCode(
        rawValue: String,
        content: BarcodeContent,
    ): ScannedCode =
        ScannedCode(
            id = CODE_ID,
            scanId = 1,
            rawValue = rawValue,
            content = content,
            format = BarcodeFormat.QR_CODE,
            scannedAt = Instant.fromEpochMilliseconds(0),
        )

    private companion object {
        const val CODE_ID = 7L
        const val WIFI_RAW_VALUE = "WIFI:T:WPA;S:Scanny;P:secret;;"

        val WIFI = BarcodeContent.Wifi(
            ssid = "Scanny",
            encryption = WifiEncryption.WPA,
            password = "secret",
        )
    }
}
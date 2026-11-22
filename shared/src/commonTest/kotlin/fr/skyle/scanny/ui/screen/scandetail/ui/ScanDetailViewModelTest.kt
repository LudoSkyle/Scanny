package fr.skyle.scanny.ui.screen.scandetail.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.fake.FakeCodeImageRenderer
import fr.skyle.scanny.fake.FakeContactAdder
import fr.skyle.scanny.fake.FakeDeleteScannedCodeUseCase
import fr.skyle.scanny.fake.FakeImageSaver
import fr.skyle.scanny.fake.FakeImageSharer
import fr.skyle.scanny.fake.FakeTextSharer
import fr.skyle.scanny.fake.FakeWatchScannedCodeUseCase
import fr.skyle.scanny.fake.FakeWifiNetworkSupport
import fr.skyle.scanny.fake.FixedClock
import fr.skyle.scanny.platform.image.SavedImage
import fr.skyle.scanny.ui.core.code.image.CodeImageExporter
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
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
                scannedDay = CalendarDay.Today,
                scannedTime = "00:00",
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
                isRawValueShown = true,
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
    fun `a network shows its raw content and a link does not show it twice`() = runTest(testDispatcher) {
        // Given
        val link = scannedCode(
            rawValue = "https://scanny.app",
            content = BarcodeContent.Url(url = "https://scanny.app"),
        )

        // When
        val networkViewModel = scanDetailViewModel()
        val linkViewModel = scanDetailViewModel(watchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = link))

        // Then
        assertEquals(
            expected = true to false,
            actual = networkViewModel.uiState.value.code?.isRawValueShown to linkViewModel.uiState.value.code?.isRawValueShown,
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

    @Test
    fun `sharing the image draws the code dark on white and shares it named after the code`() = runTest(testDispatcher) {
        // Given
        val codeImageRenderer = FakeCodeImageRenderer()
        val imageSharer = FakeImageSharer()
        val viewModel = scanDetailViewModel(
            codeImageRenderer = codeImageRenderer,
            imageSharer = imageSharer,
        )

        // When
        viewModel.onShareImageClicked()

        // Then
        assertEquals(
            expected = listOf(
                FakeCodeImageRenderer.RenderedCode(
                    rawValue = WIFI_RAW_VALUE,
                    format = BarcodeFormat.QR_CODE,
                    style = CodeStyle(),
                ),
            ),
            actual = codeImageRenderer.renderedCodes,
        )
        assertEquals(
            expected = listOf(listOf<Byte>(1, 2, 3) to "scanny-scan-7.png"),
            actual = imageSharer.sharedImages,
        )
    }

    @Test
    fun `saving the image keeps it in the photos and says it`() = runTest(testDispatcher) {
        // Given
        val imageSaver = FakeImageSaver()
        val viewModel = scanDetailViewModel(imageSaver = imageSaver)
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSaveImageClicked()

        // Then
        assertEquals(
            expected = listOf(listOf<Byte>(1, 2, 3) to "scanny-scan-7.png") to listOf<ScanDetailViewModel.ScanDetailEvent>(ScanDetailViewModel.ScanDetailEvent.ImageSaved(savedImage = SavedImage(id = "saved/scanny-scan-7.png"))),
            actual = imageSaver.savedImages to events,
        )
    }

    @Test
    fun `an image that cannot be shared or saved is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanDetailViewModel(
            imageSharer = FakeImageSharer(error = IllegalStateException("No app to share with")),
            imageSaver = FakeImageSaver(error = IllegalStateException("No access to the photos")),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onShareImageClicked()
        viewModel.onSaveImageClicked()

        // Then
        assertEquals(
            expected = listOf(
                ScanDetailViewModel.ScanDetailEvent.ImageNotShared,
                ScanDetailViewModel.ScanDetailEvent.ImageNotSaved,
            ),
            actual = events,
        )
    }

    @Test
    fun `saving the image is not offered on a device that does not let the app keep one`() = runTest(testDispatcher) {
        // When
        val viewModel = scanDetailViewModel(imageSaver = FakeImageSaver(canSave = false))

        // Then
        assertFalse(viewModel.uiState.value.canSaveImage)
    }

    private fun scanDetailViewModel(
        watchScannedCodeUseCase: FakeWatchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = wifiCode()),
        deleteScannedCodeUseCase: FakeDeleteScannedCodeUseCase = FakeDeleteScannedCodeUseCase(),
        textSharer: FakeTextSharer = FakeTextSharer(),
        codeImageRenderer: FakeCodeImageRenderer = FakeCodeImageRenderer(),
        imageSharer: FakeImageSharer = FakeImageSharer(),
        imageSaver: FakeImageSaver = FakeImageSaver(),
        contactAdder: FakeContactAdder = FakeContactAdder(),
        wifiNetworkSupport: FakeWifiNetworkSupport = FakeWifiNetworkSupport(),
    ): ScanDetailViewModel =
        ScanDetailViewModel(
            codeId = CODE_ID,
            watchScannedCodeUseCase = watchScannedCodeUseCase,
            deleteScannedCodeUseCase = deleteScannedCodeUseCase,
            textSharer = textSharer,
            codeImageExporter = CodeImageExporter(
                codeImageRenderer = codeImageRenderer,
                imageSharer = imageSharer,
                imageSaver = imageSaver,
            ),
            contactAdder = contactAdder,
            wifiNetworkSupport = wifiNetworkSupport,
            // Scanned at the very moment of the test, in UTC
            clock = FixedClock(instant = Instant.fromEpochMilliseconds(0)),
            timeZone = TimeZone.UTC,
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
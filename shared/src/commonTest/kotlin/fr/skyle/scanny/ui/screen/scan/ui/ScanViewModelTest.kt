package fr.skyle.scanny.ui.screen.scan.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.fake.FakeContactAdder
import fr.skyle.scanny.fake.FakeRecordScanUseCase
import fr.skyle.scanny.fake.FakeWatchScanSettingsUseCase
import fr.skyle.scanny.fake.FakeWifiNetworkSupport
import fr.skyle.scanny.platform.camera.CameraCapabilities
import fr.skyle.scanny.platform.gallery.ImageScanResult
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

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
    fun `a detected code is recorded and shown`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = listOf(listOf(qrCode(rawValue = "Hello"))),
            actual = recordScanUseCase.recordedScans,
        )
        assertEquals(
            expected = ScanViewModel.ScanUIState(
                isDetectionPaused = true,
                result = ScanResultItem(
                    codeId = 1,
                    type = CodeType.TEXT,
                    format = BarcodeFormat.QR_CODE,
                    summary = "Hello",
                    rawValue = "Hello",
                    action = null,
                ),
            ),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `a code detected while one is being recorded is ignored`() = runTest(testDispatcher) {
        // Given
        val recording = CompletableDeferred<Unit>()
        val recordScanUseCase = FakeRecordScanUseCase(completion = recording)
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "First"))
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "First"))
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Second"))
        recording.complete(Unit)

        // Then
        assertEquals(
            expected = listOf(listOf(qrCode(rawValue = "First"))),
            actual = recordScanUseCase.recordedScans,
        )
    }

    @Test
    fun `a code detected while one is shown is ignored`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "First"))

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Second"))

        // Then
        assertEquals(
            expected = listOf(listOf(qrCode(rawValue = "First"))),
            actual = recordScanUseCase.recordedScans,
        )
    }

    @Test
    fun `a scan vibrates when the vibration is enabled`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = true))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.Vibrate),
            actual = events,
        )
    }

    @Test
    fun `a scan does not vibrate when the vibration is disabled`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = emptyList(),
            actual = events,
        )
    }

    @Test
    fun `a link is opened when the setting asks it and the detection resumes after a pause`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(content = { rawValue -> BarcodeContent.Url(url = rawValue) }),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isOpenLinkAfterScanEnabled = true,
            ),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "https://scanny.app"))

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.OpenUrl(url = "https://scanny.app")),
            actual = events,
        )
        assertNull(viewModel.uiState.value.result)
        assertTrue(viewModel.uiState.value.isDetectionPaused)

        advanceTimeBy(ScanViewModel.LINK_OPENING_PAUSE)
        runCurrent()
        assertFalse(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `a link is shown when the setting does not ask to open it`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(content = { rawValue -> BarcodeContent.Url(url = rawValue) }),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isOpenLinkAfterScanEnabled = false),
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "https://scanny.app"))

        // Then
        assertEquals(
            expected = ScanResultItem(
                codeId = 1,
                type = CodeType.LINK,
                format = BarcodeFormat.QR_CODE,
                summary = "https://scanny.app",
                rawValue = "https://scanny.app",
                action = CodeAction.OpenLink(link = "https://scanny.app"),
            ),
            actual = viewModel.uiState.value.result,
        )
    }

    @Test
    fun `a network the device can join is shown with the action to join it`() = runTest(testDispatcher) {
        // Given
        val wifi = BarcodeContent.Wifi(
            ssid = "Scanny",
            encryption = WifiEncryption.WPA,
            password = "secret12",
        )
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(content = { wifi }),
            wifiNetworkSupport = FakeWifiNetworkSupport(canJoin = true),
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "WIFI:T:WPA;S:Scanny;P:secret12;;"))

        // Then
        assertEquals(
            expected = CodeAction.JoinWifi(wifi = wifi),
            actual = viewModel.uiState.value.result?.action,
        )
    }

    @Test
    fun `adding the contact of the result opens the contacts`() = runTest(testDispatcher) {
        // Given
        val contactAdder = FakeContactAdder()
        val viewModel = scanViewModel(contactAdder = contactAdder)
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
    fun `dismissing the result resumes the detection`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // When
        viewModel.onResultDismissed()

        // Then
        assertNull(viewModel.uiState.value.result)
        assertFalse(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `a scan that cannot be saved is reported and the detection resumes`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(error = IllegalStateException("Disk full")),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.ScanNotSaved),
            actual = events,
        )
        assertNull(viewModel.uiState.value.result)
        assertFalse(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `a code found in an image is recorded and shown`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)

        // When
        viewModel.onImageScanned(result = ImageScanResult.Detected(barcode = qrCode(rawValue = "Hello")))

        // Then
        assertEquals(
            expected = listOf(listOf(qrCode(rawValue = "Hello"))),
            actual = recordScanUseCase.recordedScans,
        )
        assertEquals(
            expected = CodeType.TEXT,
            actual = viewModel.uiState.value.result?.type,
        )
    }

    @Test
    fun `an image without code is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onImageScanned(result = ImageScanResult.NoBarcode)

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.NoCodeInImage),
            actual = events,
        )
    }

    @Test
    fun `seeing the detail of the result opens it and keeps the detection paused`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false))
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSeeDetailClicked()

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.OpenScanDetail(codeId = 1)),
            actual = events,
        )
        assertNull(viewModel.uiState.value.result)
        assertTrue(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `back from the detail the detection resumes`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onSeeDetailClicked()

        // When
        viewModel.onScreenShown()

        // Then
        assertFalse(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `the screen shown again while a result is shown keeps the detection paused`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // When
        viewModel.onScreenShown()

        // Then
        assertTrue(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `the torch is switched on and off`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()

        // When
        viewModel.onTorchClicked()
        val isTorchEnabledAfterFirstClick = viewModel.uiState.value.isTorchEnabled
        viewModel.onTorchClicked()

        // Then
        assertTrue(isTorchEnabledAfterFirstClick)
        assertFalse(viewModel.uiState.value.isTorchEnabled)
    }

    @Test
    fun `what the camera can do and its absence are kept`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        val capabilities = CameraCapabilities(
            hasTorch = true,
            minZoomRatio = 1f,
            maxZoomRatio = 10f,
        )

        // When
        viewModel.onCameraCapabilitiesChanged(capabilities = capabilities)
        viewModel.onCameraUnavailable()

        // Then
        assertEquals(
            expected = capabilities,
            actual = viewModel.uiState.value.cameraCapabilities,
        )
        assertTrue(viewModel.uiState.value.isCameraUnavailable)
    }

    private fun scanViewModel(
        recordScanUseCase: FakeRecordScanUseCase = FakeRecordScanUseCase(),
        watchScanSettingsUseCase: FakeWatchScanSettingsUseCase = FakeWatchScanSettingsUseCase(),
        contactAdder: FakeContactAdder = FakeContactAdder(),
        wifiNetworkSupport: FakeWifiNetworkSupport = FakeWifiNetworkSupport(),
    ): ScanViewModel =
        ScanViewModel(
            recordScanUseCase = recordScanUseCase,
            watchScanSettingsUseCase = watchScanSettingsUseCase,
            contactAdder = contactAdder,
            wifiNetworkSupport = wifiNetworkSupport,
        )

    private fun TestScope.collectEvents(viewModel: ScanViewModel): List<ScanViewModel.ScanEvent> {
        val events = mutableListOf<ScanViewModel.ScanEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private fun qrCode(rawValue: String): DetectedBarcode =
        DetectedBarcode(
            rawValue = rawValue,
            format = BarcodeFormat.QR_CODE,
        )
}
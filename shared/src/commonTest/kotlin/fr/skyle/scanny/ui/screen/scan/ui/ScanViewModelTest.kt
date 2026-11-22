package fr.skyle.scanny.ui.screen.scan.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.fake.FakeContactAdder
import fr.skyle.scanny.fake.FakeRecordScanUseCase
import fr.skyle.scanny.fake.FakeScanBeeper
import fr.skyle.scanny.fake.FakeWatchScanSettingsUseCase
import fr.skyle.scanny.fake.FakeWifiNetworkSupport
import fr.skyle.scanny.fake.TestClock
import fr.skyle.scanny.platform.camera.CameraCapabilities
import fr.skyle.scanny.platform.camera.CameraLens
import fr.skyle.scanny.platform.gallery.ImageScanResult
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.screen.scan.ui.model.MultipleScanSession
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResult
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testClock = TestClock()

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
                result = ScanResult.Code(
                    item = ScanResultItem(
                        codeId = 1,
                        type = CodeType.TEXT,
                        format = BarcodeFormat.QR_CODE,
                        fields = listOf(
                            CodeField(
                                label = null,
                                value = "Hello",
                            ),
                        ),
                        rawValue = "Hello",
                        action = null,
                    ),
                ),
            ),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `an sms is shown with its number and its message`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(
                content = {
                    BarcodeContent.Sms(
                        phoneNumber = "+33600000000",
                        message = "See you at eight",
                    )
                },
            ),
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "SMSTO:+33600000000:See you at eight"))

        // Then
        assertEquals(
            expected = listOf(
                CodeField(
                    label = CodeFieldLabel.PHONE_NUMBER,
                    value = "+33600000000",
                ),
                CodeField(
                    label = CodeFieldLabel.MESSAGE,
                    value = "See you at eight",
                ),
            ),
            actual = (viewModel.uiState.value.result as? ScanResult.Code)?.item?.fields,
        )
    }

    @Test
    fun `the sheet of a scan shows the text of the code when the setting asks and keeps its action`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(
                content = {
                    BarcodeContent.Sms(
                        phoneNumber = "+33600000000",
                        message = "See you at eight",
                    )
                },
            ),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isRawContentShown = true),
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "SMSTO:+33600000000:See you at eight"))

        // Then
        val item = (viewModel.uiState.value.result as? ScanResult.Code)?.item
        assertEquals(
            expected = listOf(
                CodeField(
                    label = null,
                    value = "SMSTO:+33600000000:See you at eight",
                ),
            ),
            actual = item?.fields,
        )
        assertEquals(
            expected = true,
            actual = item?.action is CodeAction.WriteSms,
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
    fun `a scan beeps when the beep is enabled`() = runTest(testDispatcher) {
        // Given
        val scanBeeper = FakeScanBeeper()
        val viewModel = scanViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isBeepAfterScanEnabled = true),
            scanBeeper = scanBeeper,
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = 1,
            actual = scanBeeper.beepCount,
        )
    }

    @Test
    fun `a scan does not beep when the beep is disabled`() = runTest(testDispatcher) {
        // Given
        val scanBeeper = FakeScanBeeper()
        val viewModel = scanViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isBeepAfterScanEnabled = false),
            scanBeeper = scanBeeper,
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = 0,
            actual = scanBeeper.beepCount,
        )
    }

    @Test
    fun `a scan copies the code when the copy is enabled`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isCopyAfterScanEnabled = true,
            ),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.CopyToClipboard(text = "Hello")),
            actual = events,
        )
    }

    @Test
    fun `a scan does not copy the code when the copy is disabled`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isCopyAfterScanEnabled = false,
            ),
        )
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
    fun `the content is opened when the setting asks it and the detection resumes after a pause`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(content = { rawValue -> BarcodeContent.Url(url = rawValue) }),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = true,
            ),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "https://scanny.app"))

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.OpenContent(action = CodeAction.OpenLink(link = "https://scanny.app"))),
            actual = events,
        )
        assertNull(viewModel.uiState.value.result)
        assertTrue(viewModel.uiState.value.isDetectionPaused)

        advanceTimeBy(ScanViewModel.CONTENT_OPENING_PAUSE)
        runCurrent()
        assertFalse(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `a contact is added when the setting asks to open the content`() = runTest(testDispatcher) {
        // Given
        val contact = BarcodeContent.Contact(formattedName = "John Doe")
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(content = { contact }),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = true,
            ),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "MECARD:N:Doe,John;;"))

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.OpenContent(action = CodeAction.AddContact(contact = contact))),
            actual = events,
        )
    }

    @Test
    fun `a network the device cannot join is shown even when the setting asks to open the content`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(
                content = {
                    BarcodeContent.Wifi(
                        ssid = "Scanny",
                        encryption = WifiEncryption.WEP,
                        password = "secret",
                    )
                },
            ),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = true,
            ),
            wifiNetworkSupport = FakeWifiNetworkSupport(canJoin = false),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "WIFI:T:WEP;S:Scanny;P:secret;;"))

        // Then
        assertEquals(
            expected = emptyList(),
            actual = events,
        )
        assertEquals(
            expected = CodeType.WIFI,
            actual = (viewModel.uiState.value.result as? ScanResult.Code)?.item?.type,
        )
    }

    @Test
    fun `a text is shown even when the setting asks to open the content`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(
                isVibrationAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = true,
            ),
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = emptyList(),
            actual = events,
        )
        assertEquals(
            expected = CodeType.TEXT,
            actual = (viewModel.uiState.value.result as? ScanResult.Code)?.item?.type,
        )
    }

    @Test
    fun `a link is shown when the setting does not ask to open it`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(content = { rawValue -> BarcodeContent.Url(url = rawValue) }),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isOpenContentAfterScanEnabled = false),
        )

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "https://scanny.app"))

        // Then
        assertEquals(
            expected = ScanResult.Code(
                item = ScanResultItem(
                    codeId = 1,
                    type = CodeType.LINK,
                    format = BarcodeFormat.QR_CODE,
                    fields = listOf(
                        CodeField(
                            label = null,
                            value = "https://scanny.app",
                        ),
                    ),
                    rawValue = "https://scanny.app",
                    action = CodeAction.OpenLink(link = "https://scanny.app"),
                ),
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
            actual = (viewModel.uiState.value.result as? ScanResult.Code)?.item?.action,
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
            actual = (viewModel.uiState.value.result as? ScanResult.Code)?.item?.type,
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
        viewModel.onScreenPaused()

        // When
        viewModel.onScreenResumed()

        // Then
        assertFalse(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `the screen shown again while a result is shown keeps the detection paused`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onScreenPaused()

        // When
        viewModel.onScreenResumed()

        // Then
        assertTrue(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `the same code read again just after the result is dismissed is ignored`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onResultDismissed()

        // When
        testClock.advanceBy(1.seconds)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = 1,
            actual = recordScanUseCase.recordedScans.size,
        )
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun `the same code read again after the pause that follows the result is recorded`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onResultDismissed()

        // When
        testClock.advanceBy(ScanViewModel.SAME_CODE_PAUSE)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = 2,
            actual = recordScanUseCase.recordedScans.size,
        )
    }

    @Test
    fun `another code read just after the result is dismissed is recorded`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onResultDismissed()

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "World"))

        // Then
        assertEquals(
            expected = listOf(
                listOf(qrCode(rawValue = "Hello")),
                listOf(qrCode(rawValue = "World")),
            ),
            actual = recordScanUseCase.recordedScans,
        )
    }

    @Test
    fun `the same code read again just after coming back to the app is ignored`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onResultDismissed()
        testClock.advanceBy(10.seconds)
        viewModel.onScreenPaused()

        // When
        viewModel.onScreenResumed()
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = 1,
            actual = recordScanUseCase.recordedScans.size,
        )
    }

    @Test
    fun `no code is read while another app is in front`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)

        // When
        viewModel.onScreenPaused()
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))

        // Then
        assertEquals(
            expected = emptyList(),
            actual = recordScanUseCase.recordedScans,
        )
    }

    @Test
    fun `a code found in an image is recorded even just after the camera read it`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Hello"))
        viewModel.onResultDismissed()

        // When
        viewModel.onImageScanned(result = ImageScanResult.Detected(barcode = qrCode(rawValue = "Hello")))

        // Then
        assertEquals(
            expected = 2,
            actual = recordScanUseCase.recordedScans.size,
        )
    }

    @Test
    fun `a multiple scan counts each code once and keeps scanning`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onMultipleScanClicked()

        // When
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "Second"))

        // Then
        assertEquals(
            expected = ScanViewModel.ScanUIState(multipleScan = MultipleScanSession(codeCount = 2)),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `each new code of a multiple scan vibrates`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = true))
        viewModel.onMultipleScanClicked()
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "Second"))

        // Then
        assertEquals(
            expected = listOf(
                ScanViewModel.ScanEvent.Vibrate,
                ScanViewModel.ScanEvent.Vibrate,
            ),
            actual = events,
        )
    }

    @Test
    fun `each new code of a multiple scan beeps`() = runTest(testDispatcher) {
        // Given
        val scanBeeper = FakeScanBeeper()
        val viewModel = scanViewModel(
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isBeepAfterScanEnabled = true),
            scanBeeper = scanBeeper,
        )
        viewModel.onMultipleScanClicked()

        // When
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "Second"))

        // Then
        assertEquals(
            expected = 2,
            actual = scanBeeper.beepCount,
        )
    }

    @Test
    fun `finishing a multiple scan records its codes as one capture and shows them`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "Second"))

        // When
        viewModel.onMultipleScanFinished()

        // Then
        assertEquals(
            expected = listOf(
                listOf(
                    qrCode(rawValue = "First"),
                    qrCode(rawValue = "Second"),
                ),
            ),
            actual = recordScanUseCase.recordedScans,
        )
        assertEquals(
            expected = ScanViewModel.ScanUIState(
                isDetectionPaused = true,
                result = ScanResult.Group(
                    scanId = 1,
                    codes = listOf(
                        CodeRowItem(
                            codeId = 1,
                            type = CodeType.TEXT,
                            summary = "First",
                        ),
                        CodeRowItem(
                            codeId = 2,
                            type = CodeType.TEXT,
                            summary = "Second",
                        ),
                    ),
                ),
            ),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `a multiple scan that found a single code shows it as a simple scan`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "Alone"))

        // When
        viewModel.onMultipleScanFinished()

        // Then
        assertEquals(
            expected = "Alone",
            actual = (viewModel.uiState.value.result as? ScanResult.Code)?.item?.rawValue,
        )
    }

    @Test
    fun `a multiple scan without code cannot be finished`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onMultipleScanClicked()

        // When
        viewModel.onMultipleScanFinished()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = recordScanUseCase.recordedScans,
        )
        assertEquals(
            expected = MultipleScanSession(codeCount = 0),
            actual = viewModel.uiState.value.multipleScan,
        )
    }

    @Test
    fun `stopping a multiple scan with codes asks to discard them first`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false))
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onMultipleScanClicked()

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.ConfirmCodesDiscard),
            actual = events,
        )
        assertEquals(
            expected = MultipleScanSession(codeCount = 1),
            actual = viewModel.uiState.value.multipleScan,
        )
    }

    @Test
    fun `discarding a multiple scan forgets its codes`() = runTest(testDispatcher) {
        // Given
        val recordScanUseCase = FakeRecordScanUseCase()
        val viewModel = scanViewModel(recordScanUseCase = recordScanUseCase)
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "Forgotten"))

        // When
        viewModel.onMultipleScanDiscarded()
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "Kept"))
        viewModel.onMultipleScanFinished()

        // Then
        assertEquals(
            expected = listOf(listOf(qrCode(rawValue = "Kept"))),
            actual = recordScanUseCase.recordedScans,
        )
    }

    @Test
    fun `stopping an empty multiple scan ends it at once`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onMultipleScanClicked()

        // When
        viewModel.onMultipleScanClicked()

        // Then
        assertNull(viewModel.uiState.value.multipleScan)
    }

    @Test
    fun `a multiple scan that cannot be saved keeps its codes to try again`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(
            recordScanUseCase = FakeRecordScanUseCase(error = IllegalStateException("Disk full")),
            watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false),
        )
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onMultipleScanFinished()

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.ScanNotSaved),
            actual = events,
        )
        assertEquals(
            expected = ScanViewModel.ScanUIState(multipleScan = MultipleScanSession(codeCount = 1)),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `seeing the group of a multiple scan opens it`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false))
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "Second"))
        viewModel.onMultipleScanFinished()
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSeeGroupClicked()

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.OpenScanGroup(scanId = 1)),
            actual = events,
        )
        assertNull(viewModel.uiState.value.result)
        assertTrue(viewModel.uiState.value.isDetectionPaused)
    }

    @Test
    fun `a code of the group of a multiple scan opens its detail`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel(watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isVibrationAfterScanEnabled = false))
        viewModel.onMultipleScanClicked()
        viewModel.readSteadily(barcode = qrCode(rawValue = "First"))
        viewModel.readSteadily(barcode = qrCode(rawValue = "Second"))
        viewModel.onMultipleScanFinished()
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onResultCodeClicked(codeId = 2)

        // Then
        assertEquals(
            expected = listOf(ScanViewModel.ScanEvent.OpenScanDetail(codeId = 2)),
            actual = events,
        )
    }

    @Test
    fun `a code read only once by a multiple scan is not counted`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onMultipleScanClicked()

        // When
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Misread"))

        // Then
        assertEquals(
            expected = MultipleScanSession(codeCount = 0),
            actual = viewModel.uiState.value.multipleScan,
        )
    }

    @Test
    fun `a code read again too late is not counted`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onMultipleScanClicked()
        repeat(ScanViewModel.CONFIRMATION_READINGS - 1) {
            viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Misread"))
        }

        // When
        testClock.advanceBy(ScanViewModel.CONFIRMATION_WINDOW + 1.milliseconds)
        viewModel.onBarcodeDetected(barcode = qrCode(rawValue = "Misread"))

        // Then
        assertEquals(
            expected = MultipleScanSession(codeCount = 0),
            actual = viewModel.uiState.value.multipleScan,
        )
    }

    @Test
    fun `a code found in an image is counted at once by a multiple scan`() = runTest(testDispatcher) {
        // Given
        val viewModel = scanViewModel()
        viewModel.onMultipleScanClicked()

        // When
        viewModel.onImageScanned(result = ImageScanResult.Detected(barcode = qrCode(rawValue = "From an image")))

        // Then
        assertEquals(
            expected = MultipleScanSession(codeCount = 1),
            actual = viewModel.uiState.value.multipleScan,
        )
    }

    @Test
    fun `the camera follows the setting of the front camera`() = runTest(testDispatcher) {
        // Given
        val watchScanSettingsUseCase = FakeWatchScanSettingsUseCase(isFrontCameraUsed = false)
        val viewModel = scanViewModel(watchScanSettingsUseCase = watchScanSettingsUseCase)

        // When
        watchScanSettingsUseCase.settings.value = watchScanSettingsUseCase.settings.value.copy(isFrontCameraUsed = true)

        // Then
        assertEquals(
            expected = CameraLens.FRONT,
            actual = viewModel.uiState.value.cameraLens,
        )
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
        scanBeeper: FakeScanBeeper = FakeScanBeeper(),
    ): ScanViewModel =
        ScanViewModel(
            recordScanUseCase = recordScanUseCase,
            watchScanSettingsUseCase = watchScanSettingsUseCase,
            contactAdder = contactAdder,
            wifiNetworkSupport = wifiNetworkSupport,
            scanBeeper = scanBeeper,
            clock = testClock,
        )

    private fun TestScope.collectEvents(viewModel: ScanViewModel): List<ScanViewModel.ScanEvent> {
        val events = mutableListOf<ScanViewModel.ScanEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    // As the camera reads a code held in front of it
    private fun ScanViewModel.readSteadily(barcode: DetectedBarcode) {
        repeat(ScanViewModel.CONFIRMATION_READINGS) {
            onBarcodeDetected(barcode = barcode)
        }
    }

    private fun qrCode(rawValue: String): DetectedBarcode =
        DetectedBarcode(
            rawValue = rawValue,
            format = BarcodeFormat.QR_CODE,
        )
}
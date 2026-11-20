package fr.skyle.scanny.ui.screen.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCase
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import fr.skyle.scanny.platform.camera.CameraCapabilities
import fr.skyle.scanny.platform.contact.ContactAdder
import fr.skyle.scanny.platform.gallery.ImageScanResult
import fr.skyle.scanny.platform.wifi.WifiNetworkSupport
import fr.skyle.scanny.ui.screen.scan.ui.mapper.toScanResult
import fr.skyle.scanny.ui.screen.scan.ui.mapper.toScanResultItem
import fr.skyle.scanny.ui.screen.scan.ui.model.MultipleScanSession
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class ScanViewModel(
    private val recordScanUseCase: RecordScanUseCase,
    private val watchScanSettingsUseCase: WatchScanSettingsUseCase,
    private val contactAdder: ContactAdder,
    private val wifiNetworkSupport: WifiNetworkSupport,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUIState())
    val uiState: StateFlow<ScanUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var isAwayFromScan = false

    // The codes of the multiple scan in progress, in their order, each once
    private val multipleScanBarcodes = mutableListOf<DetectedBarcode>()

    fun onBarcodeDetected(barcode: DetectedBarcode) {
        if (_uiState.value.multipleScan != null) {
            onMultipleScanBarcodeDetected(barcode = barcode)
        } else {
            onSimpleScanBarcodeDetected(barcode = barcode)
        }
    }

    private fun onSimpleScanBarcodeDetected(barcode: DetectedBarcode) {
        // Paused before anything else: the camera reads the same code several times in a row
        if (_uiState.value.isDetectionPaused) {
            return
        }
        _uiState.update { uiState ->
            uiState.copy(isDetectionPaused = true)
        }

        viewModelScope.launch {
            try {
                val settings = watchScanSettingsUseCase().first()
                if (settings.isVibrationAfterScanEnabled) {
                    _event.send(ScanEvent.Vibrate)
                }

                // A simple scan is a capture of the one code read
                val scannedCode = recordScanUseCase(barcodes = listOf(barcode)).codes.single()

                val content = scannedCode.content
                if (content is BarcodeContent.Url && settings.isOpenLinkAfterScanEnabled) {
                    _event.send(ScanEvent.OpenUrl(url = content.url))
                    // The browser takes a moment to open: meanwhile the camera would read the code again
                    delay(LINK_OPENING_PAUSE)
                    resumeDetection()
                } else {
                    _uiState.update { uiState ->
                        uiState.copy(
                            result = ScanResult.Code(item = scannedCode.toScanResultItem(canJoinWifi = wifiNetworkSupport::canJoin)),
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to record the scan of a ${barcode.format}" }
                _event.send(ScanEvent.ScanNotSaved)
                resumeDetection()
            }
        }
    }

    // The camera reads the same codes again and again while they are in front of it: each counts once
    private fun onMultipleScanBarcodeDetected(barcode: DetectedBarcode) {
        val session = _uiState.value.multipleScan ?: return
        if (session.isRecording || barcode in multipleScanBarcodes) {
            return
        }

        multipleScanBarcodes += barcode
        _uiState.update { uiState ->
            uiState.copy(multipleScan = session.copy(codeCount = multipleScanBarcodes.size))
        }
        viewModelScope.launch {
            if (watchScanSettingsUseCase().first().isVibrationAfterScanEnabled) {
                _event.send(ScanEvent.Vibrate)
            }
        }
    }

    fun onMultipleScanClicked() {
        val session = _uiState.value.multipleScan
        when {
            session == null -> {
                multipleScanBarcodes.clear()
                _uiState.update { uiState ->
                    uiState.copy(
                        multipleScan = MultipleScanSession(codeCount = 0),
                        isDetectionPaused = false,
                    )
                }
            }

            session.isRecording -> Unit

            // Codes would be lost: the user confirms first
            session.codeCount > 0 -> viewModelScope.launch {
                _event.send(ScanEvent.ConfirmCodesDiscard)
            }

            else -> onMultipleScanDiscarded()
        }
    }

    fun onMultipleScanDiscarded() {
        multipleScanBarcodes.clear()
        _uiState.update { uiState ->
            uiState.copy(multipleScan = null)
        }
    }

    fun onMultipleScanFinished() {
        val session = _uiState.value.multipleScan ?: return
        if (session.isRecording || multipleScanBarcodes.isEmpty()) {
            return
        }

        _uiState.update { uiState ->
            uiState.copy(
                multipleScan = session.copy(isRecording = true),
                isDetectionPaused = true,
            )
        }

        viewModelScope.launch {
            try {
                val scan = recordScanUseCase(barcodes = multipleScanBarcodes.toList())

                multipleScanBarcodes.clear()
                _uiState.update { uiState ->
                    uiState.copy(
                        multipleScan = null,
                        result = scan.toScanResult(canJoinWifi = wifiNetworkSupport::canJoin),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to record a multiple scan of ${multipleScanBarcodes.size} codes" }
                _event.send(ScanEvent.ScanNotSaved)
                // The codes found stay, to try again
                _uiState.update { uiState ->
                    uiState.copy(
                        multipleScan = session.copy(isRecording = false),
                        isDetectionPaused = false,
                    )
                }
            }
        }
    }

    fun onImageScanned(result: ImageScanResult) {
        when (result) {
            is ImageScanResult.Detected -> onBarcodeDetected(barcode = result.barcode)
            ImageScanResult.NoBarcode -> viewModelScope.launch {
                _event.send(ScanEvent.NoCodeInImage)
            }
        }
    }

    fun onResultDismissed() {
        _uiState.update { uiState ->
            uiState.copy(
                result = null,
                isDetectionPaused = false,
            )
        }
    }

    fun onAddContactClicked(contact: BarcodeContent.Contact) {
        try {
            contactAdder.addContact(contact = contact)
        } catch (exception: Exception) {
            // Under the sheet, a message would not be seen: the failure is only logged
            Logger.e(throwable = exception) { "Failed to open the addition of a scanned contact" }
        }
    }

    fun onSeeDetailClicked() {
        val result = _uiState.value.result as? ScanResult.Code ?: return
        leaveScan(event = ScanEvent.OpenScanDetail(codeId = result.item.codeId))
    }

    fun onResultCodeClicked(codeId: Long) {
        leaveScan(event = ScanEvent.OpenScanDetail(codeId = codeId))
    }

    fun onSeeGroupClicked() {
        val result = _uiState.value.result as? ScanResult.Group ?: return
        leaveScan(event = ScanEvent.OpenScanGroup(scanId = result.scanId))
    }

    fun onScreenShown() {
        if (isAwayFromScan) {
            isAwayFromScan = false
            resumeDetection()
        }
    }

    fun onTorchClicked() {
        _uiState.update { uiState ->
            uiState.copy(isTorchEnabled = !uiState.isTorchEnabled)
        }
    }

    fun onCameraCapabilitiesChanged(capabilities: CameraCapabilities) {
        _uiState.update { uiState ->
            uiState.copy(cameraCapabilities = capabilities)
        }
    }

    fun onCameraUnavailable() {
        _uiState.update { uiState ->
            uiState.copy(isCameraUnavailable = true)
        }
    }

    // Still paused: the camera keeps reading during the transition to the next screen
    private fun leaveScan(event: ScanEvent) {
        isAwayFromScan = true
        _uiState.update { uiState ->
            uiState.copy(result = null)
        }
        viewModelScope.launch {
            _event.send(event)
        }
    }

    private fun resumeDetection() {
        _uiState.update { uiState ->
            uiState.copy(isDetectionPaused = false)
        }
    }

    data class ScanUIState(
        val isDetectionPaused: Boolean = false,
        val isTorchEnabled: Boolean = false,
        val result: ScanResult? = null,
        val cameraCapabilities: CameraCapabilities? = null,
        val isCameraUnavailable: Boolean = false,
        // Null outside of a multiple scan
        val multipleScan: MultipleScanSession? = null,
    )

    sealed interface ScanEvent {
        data object Vibrate : ScanEvent
        data class OpenUrl(val url: String) : ScanEvent
        data object NoCodeInImage : ScanEvent
        data object ScanNotSaved : ScanEvent
        data class OpenScanDetail(val codeId: Long) : ScanEvent
        data class OpenScanGroup(val scanId: Long) : ScanEvent
        data object ConfirmCodesDiscard : ScanEvent
    }

    internal companion object {
        val LINK_OPENING_PAUSE = 2.seconds
    }
}
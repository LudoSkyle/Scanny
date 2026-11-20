package fr.skyle.scanny.ui.screen.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCase
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import fr.skyle.scanny.platform.camera.CameraCapabilities
import fr.skyle.scanny.platform.gallery.ImageScanResult
import fr.skyle.scanny.ui.screen.scan.ui.mapper.toScanResultItem
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUIState())
    val uiState: StateFlow<ScanUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var isAwayOnDetail = false

    fun onBarcodeDetected(barcode: DetectedBarcode) {
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
                        uiState.copy(result = scannedCode.toScanResultItem())
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

    fun onSeeDetailClicked() {
        val codeId = _uiState.value.result?.codeId ?: return

        // Still paused: the camera keeps reading during the transition to the detail
        isAwayOnDetail = true
        _uiState.update { uiState ->
            uiState.copy(result = null)
        }
        viewModelScope.launch {
            _event.send(ScanEvent.OpenScanDetail(codeId = codeId))
        }
    }

    fun onScreenShown() {
        if (isAwayOnDetail) {
            isAwayOnDetail = false
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

    private fun resumeDetection() {
        _uiState.update { uiState ->
            uiState.copy(isDetectionPaused = false)
        }
    }

    data class ScanUIState(
        val isDetectionPaused: Boolean = false,
        val isTorchEnabled: Boolean = false,
        val result: ScanResultItem? = null,
        val cameraCapabilities: CameraCapabilities? = null,
        val isCameraUnavailable: Boolean = false,
    )

    sealed interface ScanEvent {
        data object Vibrate : ScanEvent
        data class OpenUrl(val url: String) : ScanEvent
        data object NoCodeInImage : ScanEvent
        data object ScanNotSaved : ScanEvent
        data class OpenScanDetail(val codeId: Long) : ScanEvent
    }

    internal companion object {
        val LINK_OPENING_PAUSE = 2.seconds
    }
}
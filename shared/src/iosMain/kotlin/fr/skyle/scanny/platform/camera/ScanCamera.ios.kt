package fr.skyle.scanny.platform.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import fr.skyle.scanny.domain.model.DetectedBarcode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.exposurePointOfInterestSupported
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.focusPointOfInterestSupported
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.maxAvailableVideoZoomFactor
import platform.AVFoundation.minAvailableVideoZoomFactor
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun ScanCamera(
    isDetectionEnabled: Boolean,
    isTorchEnabled: Boolean,
    zoomRatio: Float,
    focusRequest: FocusRequest?,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCapabilitiesChanged: (CameraCapabilities) -> Unit,
    onCameraUnavailable: () -> Unit,
    modifier: Modifier,
) {
    if (SimulatedScan.isEnabled) {
        SimulatedScanCamera(
            modifier = modifier,
            isDetectionEnabled = isDetectionEnabled,
            takeBarcode = SimulatedScan::take,
            onBarcodeDetected = onBarcodeDetected,
            onCapabilitiesChanged = onCapabilitiesChanged,
        )
    } else {
        DeviceScanCamera(
            modifier = modifier,
            isDetectionEnabled = isDetectionEnabled,
            isTorchEnabled = isTorchEnabled,
            zoomRatio = zoomRatio,
            focusRequest = focusRequest,
            onBarcodeDetected = onBarcodeDetected,
            onCapabilitiesChanged = onCapabilitiesChanged,
            onCameraUnavailable = onCameraUnavailable,
        )
    }
}

@Composable
private fun DeviceScanCamera(
    isDetectionEnabled: Boolean,
    isTorchEnabled: Boolean,
    zoomRatio: Float,
    focusRequest: FocusRequest?,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCapabilitiesChanged: (CameraCapabilities) -> Unit,
    onCameraUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentIsDetectionEnabled by rememberUpdatedState(isDetectionEnabled)
    val currentOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)
    val camera = remember { IosScanCamera() }

    DisposableEffect(camera) {
        val capabilities = camera.start { detectedBarcode ->
            if (currentIsDetectionEnabled) {
                currentOnBarcodeDetected(detectedBarcode)
            }
        }
        if (capabilities != null) {
            onCapabilitiesChanged(capabilities)
        } else {
            onCameraUnavailable()
        }

        onDispose {
            camera.stop()
        }
    }

    LaunchedEffect(camera, isTorchEnabled) {
        camera.setTorchEnabled(isEnabled = isTorchEnabled)
    }

    LaunchedEffect(camera, zoomRatio) {
        camera.setZoomRatio(ratio = zoomRatio)
    }

    LaunchedEffect(camera, focusRequest) {
        focusRequest?.let { request ->
            camera.focus(
                x = request.x,
                y = request.y,
            )
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            camera.previewView
        },
        // The gestures on the preview (zoom, focus) are those of Compose, drawn over it
        properties = UIKitInteropProperties(interactionMode = null),
    )
}

@OptIn(ExperimentalForeignApi::class)
private class IosScanCamera {

    private val session = AVCaptureSession()
    private val metadataOutput = AVCaptureMetadataOutput()
    private var device: AVCaptureDevice? = null

    // AVFoundation keeps a weak reference to its delegate
    private var metadataDelegate: MetadataDelegate? = null

    val previewView = CameraPreviewView(session = session)

    // Null when the device has no back camera, as the simulator
    fun start(onBarcodeDetected: (DetectedBarcode) -> Unit): CameraCapabilities? {
        val backCamera = AVCaptureDevice.defaultDeviceWithDeviceType(
            deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionBack,
        ) ?: return null
        val input = AVCaptureDeviceInput.deviceInputWithDevice(
            device = backCamera,
            error = null,
        ) ?: return null

        val delegate = MetadataDelegate(onBarcodeDetected = onBarcodeDetected)
        session.beginConfiguration()
        if (session.canAddInput(input)) {
            session.addInput(input)
        }
        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)
        }
        metadataOutput.setMetadataObjectsDelegate(
            objectsDelegate = delegate,
            queue = dispatch_get_main_queue(),
        )
        val availableTypes = metadataOutput.availableMetadataObjectTypes
        metadataOutput.metadataObjectTypes = scannedMetadataObjectTypes.filter { type -> type in availableTypes }
        session.commitConfiguration()

        device = backCamera
        metadataDelegate = delegate

        // Starting a session blocks until the camera runs: never on the main thread
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            session.startRunning()
        }

        return CameraCapabilities(
            hasTorch = backCamera.hasTorch,
            minZoomRatio = backCamera.minAvailableVideoZoomFactor.toFloat(),
            maxZoomRatio = backCamera.maxAvailableVideoZoomFactor.toFloat(),
        )
    }

    fun stop() {
        setTorchEnabled(isEnabled = false)
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            session.stopRunning()
        }
    }

    fun setTorchEnabled(isEnabled: Boolean) {
        configure { camera ->
            if (camera.hasTorch) {
                camera.torchMode = if (isEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
            }
        }
    }

    fun setZoomRatio(ratio: Float) {
        configure { camera ->
            camera.videoZoomFactor = ratio.toDouble().coerceIn(
                minimumValue = camera.minAvailableVideoZoomFactor,
                maximumValue = camera.maxAvailableVideoZoomFactor,
            )
        }
    }

    fun focus(
        x: Float,
        y: Float,
    ) {
        // From the bounds of the preview to the coordinates of the camera, crop included
        val point = previewView.bounds.useContents {
            previewView.previewLayer.captureDevicePointOfInterestForPoint(
                CGPointMake(
                    x = x * size.width,
                    y = y * size.height,
                ),
            )
        }

        configure { camera ->
            if (camera.focusPointOfInterestSupported && camera.isFocusModeSupported(AVCaptureFocusModeAutoFocus)) {
                camera.focusPointOfInterest = point
                camera.focusMode = AVCaptureFocusModeAutoFocus
            }
            if (camera.exposurePointOfInterestSupported && camera.isExposureModeSupported(AVCaptureExposureModeAutoExpose)) {
                camera.exposurePointOfInterest = point
                camera.exposureMode = AVCaptureExposureModeAutoExpose
            }
        }
    }

    private fun configure(block: (AVCaptureDevice) -> Unit) {
        val camera = device ?: return
        if (camera.lockForConfiguration(null)) {
            block(camera)
            camera.unlockForConfiguration()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class CameraPreviewView(
    session: AVCaptureSession,
) : UIView(frame = CGRectZero.readValue()) {

    val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        // Without the implicit animation of a layer, which would lag behind the resized view
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

private class MetadataDelegate(
    private val onBarcodeDetected: (DetectedBarcode) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        // Every code of the frame: a multiple scan keeps them all, a simple one the first
        didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .mapNotNull { code ->
                val rawValue = code.stringValue ?: return@mapNotNull null
                val format = metadataObjectTypeToBarcodeFormat(type = code.type) ?: return@mapNotNull null
                DetectedBarcode(
                    rawValue = rawValue,
                    format = format,
                )
            }
            .forEach(onBarcodeDetected)
    }
}
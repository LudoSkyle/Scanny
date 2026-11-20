package fr.skyle.scanny.platform.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.touchlab.kermit.Logger
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.awaitCancellation
import java.util.concurrent.Executors

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
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(color = Color.Black))
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentIsDetectionEnabled by rememberUpdatedState(isDetectionEnabled)
    val currentOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)
    val currentOnCapabilitiesChanged by rememberUpdatedState(onCapabilitiesChanged)
    val currentOnCameraUnavailable by rememberUpdatedState(onCameraUnavailable)

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val coordinateTransformer = remember { MutableCoordinateTransformer() }

    LaunchedEffect(lifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val barcodeScanner = BarcodeScanning.getClient(mlKitScannerOptions)
        val analysisExecutor = Executors.newSingleThreadExecutor()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                surfaceRequest = request
            }
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(
            analysisExecutor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                ContextCompat.getMainExecutor(context),
            ) { result ->
                if (currentIsDetectionEnabled) {
                    result.getValue(barcodeScanner)
                        ?.firstNotNullOfOrNull { barcode -> barcode.toDetectedBarcode() }
                        ?.let { detectedBarcode -> currentOnBarcodeDetected(detectedBarcode) }
                }
            },
        )

        var zoomState: LiveData<ZoomState>? = null
        var lastCapabilities: CameraCapabilities? = null
        val zoomStateObserver = Observer<ZoomState> { state ->
            val capabilities = CameraCapabilities(
                hasTorch = camera?.cameraInfo?.hasFlashUnit() == true,
                minZoomRatio = state.minZoomRatio,
                maxZoomRatio = state.maxZoomRatio,
            )
            if (capabilities != lastCapabilities) {
                lastCapabilities = capabilities
                currentOnCapabilitiesChanged(capabilities)
            }
        }

        try {
            val boundCamera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
            camera = boundCamera
            zoomState = boundCamera.cameraInfo.zoomState.also { liveData ->
                liveData.observe(lifecycleOwner, zoomStateObserver)
            }
            awaitCancellation()
        } catch (exception: IllegalArgumentException) {
            // The device has no back camera
            Logger.w(throwable = exception) { "No camera to scan with" }
            currentOnCameraUnavailable()
        } finally {
            zoomState?.removeObserver(zoomStateObserver)
            cameraProvider.unbind(preview, imageAnalysis)
            camera = null
            barcodeScanner.close()
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(camera, isTorchEnabled) {
        val boundCamera = camera ?: return@LaunchedEffect
        if (boundCamera.cameraInfo.hasFlashUnit()) {
            boundCamera.cameraControl.enableTorch(isTorchEnabled)
        }
    }

    LaunchedEffect(camera, zoomRatio) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
    }

    LaunchedEffect(camera, focusRequest) {
        val boundCamera = camera ?: return@LaunchedEffect
        val request = focusRequest ?: return@LaunchedEffect
        val resolution = surfaceRequest?.resolution ?: return@LaunchedEffect

        // From the bounds of the preview to the coordinates of the camera image, crop included
        val surfacePoint = with(coordinateTransformer) {
            Offset(
                x = request.x * size.width,
                y = request.y * size.height,
            ).transform()
        }
        val meteringPoint = SurfaceOrientedMeteringPointFactory(
            resolution.width.toFloat(),
            resolution.height.toFloat(),
        ).createPoint(surfacePoint.x, surfacePoint.y)

        boundCamera.cameraControl.startFocusAndMetering(FocusMeteringAction.Builder(meteringPoint).build())
    }

    Box(
        modifier = modifier
            .background(color = Color.Black)
            .onSizeChanged { newSize ->
                size = newSize
            },
    ) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                modifier = Modifier.fillMaxSize(),
                surfaceRequest = request,
                coordinateTransformer = coordinateTransformer,
            )
        }
    }
}
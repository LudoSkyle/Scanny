package fr.skyle.scanny.platform.camera

import android.util.Size
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.ZoomState
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
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
import fr.skyle.scanny.domain.model.DetectedBarcode
import kotlinx.coroutines.awaitCancellation
import java.util.concurrent.Executors

/**
 * The images ML Kit reads: 1920x1440 rather than the 640x480 of CameraX, for their center to keep its
 * details once cut out (see [BarcodeAnalyzer]); the whole image of the camera reads no farther. In
 * 4:3, the whole field of the camera: wider than the preview, which is cropped to fill the screen, so
 * the codes it shows are all read.
 */
private val analysisResolutionSelector = ResolutionSelector.Builder()
    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
    .setResolutionStrategy(
        ResolutionStrategy(
            Size(1920, 1440),
            ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER,
        ),
    )
    .build()

/**
 * The preview: 1920x1080 in 16:9, rather than the 1440x1080 in 4:3 of CameraX. Cropped to fill a
 * phone screen, the 4:3 image is enlarged about 1.6 times and looks blurred; the 16:9 one about 1.2
 * times. Along with the analysis, a pair of sizes every device supports.
 */
private val previewResolutionSelector = ResolutionSelector.Builder()
    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
    .setResolutionStrategy(
        ResolutionStrategy(
            Size(1920, 1080),
            ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER,
        ),
    )
    .build()

@Composable
actual fun ScanCamera(
    lens: CameraLens,
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

    LaunchedEffect(lifecycleOwner, lens) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val barcodeScanner = BarcodeScanning.getClient(mlKitScannerOptions)
        val analysisExecutor = Executors.newSingleThreadExecutor()

        val preview = Preview.Builder()
            .setResolutionSelector(previewResolutionSelector)
            .build()
            .apply {
                setSurfaceProvider { request ->
                    surfaceRequest = request
                }
            }
        val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(analysisResolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(
            analysisExecutor,
            BarcodeAnalyzer(
                barcodeScanner = barcodeScanner,
                resultExecutor = ContextCompat.getMainExecutor(context),
            ) { barcodes ->
                if (currentIsDetectionEnabled) {
                    // Every code of the image: a multiple scan keeps them all, a simple one the first
                    barcodes
                        .mapNotNull { barcode -> barcode.toDetectedBarcode() }
                        .forEach { detectedBarcode -> currentOnBarcodeDetected(detectedBarcode) }
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
                when (lens) {
                    CameraLens.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                    CameraLens.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                },
                preview,
                imageAnalysis,
            )
            camera = boundCamera
            zoomState = boundCamera.cameraInfo.zoomState.also { liveData ->
                liveData.observe(lifecycleOwner, zoomStateObserver)
            }
            awaitCancellation()
        } catch (exception: IllegalArgumentException) {
            // The device has no camera on this side
            Logger.w(throwable = exception) { "No $lens camera to scan with" }
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
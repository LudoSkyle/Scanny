package fr.skyle.scanny.platform.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.domain.model.DetectedBarcode

/** What the camera of the device can do, known once it is open. */
data class CameraCapabilities(
    val hasTorch: Boolean,
    val minZoomRatio: Float,
    val maxZoomRatio: Float,
)

/**
 * A request to focus on a point of the preview, given as a fraction of its width and height. [id]
 * tells two taps on the same point apart.
 */
data class FocusRequest(
    val x: Float,
    val y: Float,
    val id: Int,
)

/** The camera the scan uses. */
enum class CameraLens {
    BACK,
    FRONT,
}

/**
 * The preview of the camera of [lens], filling its bounds, and the detection of the codes it sees. The
 * screen gives the state to apply, the camera reports what only the device knows. Another [lens]
 * opens the camera again.
 */
@Composable
expect fun ScanCamera(
    lens: CameraLens,
    isDetectionEnabled: Boolean,
    isTorchEnabled: Boolean,
    zoomRatio: Float,
    focusRequest: FocusRequest?,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCapabilitiesChanged: (CameraCapabilities) -> Unit,
    onCameraUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
)
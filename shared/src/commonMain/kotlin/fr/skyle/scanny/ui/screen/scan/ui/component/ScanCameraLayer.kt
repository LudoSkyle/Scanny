package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.platform.camera.CameraCapabilities
import fr.skyle.scanny.platform.camera.CameraLens
import fr.skyle.scanny.platform.camera.FocusRequest
import fr.skyle.scanny.platform.camera.ScanCamera
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.place_code_in_frame
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import kotlinx.coroutines.delay
import kotlin.math.abs
import org.jetbrains.compose.resources.stringResource

// Beyond, the digital zoom only enlarges the noise of the image
private const val MAX_ZOOM_RATIO = 8f
private const val FRAME_SIZE_FRACTION = 0.7f
private const val FOCUS_INDICATOR_DURATION_MILLIS = 800L

// Closer to 1, the zoom reads 1.0×: the camera as it opens
private const val UNZOOMED_TOLERANCE = 0.05f

/**
 * The camera, the frame where to place a code, and the gestures on the preview: a pinch zooms, a tap
 * focuses on the point touched. Once zoomed, the zoom shows at the bottom of the frame, and goes back
 * to 1× when touched.
 */
@Composable
fun ScanCameraLayer(
    hint: String,
    lens: CameraLens,
    isDetectionEnabled: Boolean,
    isTorchEnabled: Boolean,
    capabilities: CameraCapabilities?,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCapabilitiesChanged: (CameraCapabilities) -> Unit,
    onCameraUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoomRatio by rememberSaveable { mutableFloatStateOf(1f) }
    var focusRequest by remember { mutableStateOf<FocusRequest?>(null) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val currentCapabilities by rememberUpdatedState(capabilities)

    val scrimColor = SCTheme.colors.backgroundBlack.copy(alpha = 0.5f)
    val frameColor = SCTheme.colors.textLight

    LaunchedEffect(focusRequest) {
        if (focusRequest != null) {
            delay(FOCUS_INDICATOR_DURATION_MILLIS)
            focusPoint = null
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val frameSize = minOf(maxWidth, maxHeight) * FRAME_SIZE_FRACTION

        ScanCamera(
            modifier = Modifier.fillMaxSize(),
            lens = lens,
            isDetectionEnabled = isDetectionEnabled,
            isTorchEnabled = isTorchEnabled,
            zoomRatio = zoomRatio,
            focusRequest = focusRequest,
            onBarcodeDetected = onBarcodeDetected,
            onCapabilitiesChanged = onCapabilitiesChanged,
            onCameraUnavailable = onCameraUnavailable,
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Offscreen, so that the frame can clear the scrim drawn under it
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focusPoint = offset
                        focusRequest = FocusRequest(
                            x = offset.x / size.width,
                            y = offset.y / size.height,
                            id = (focusRequest?.id ?: 0) + 1,
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        val cameraCapabilities = currentCapabilities ?: return@detectTransformGestures
                        zoomRatio = (zoomRatio * zoomChange).coerceIn(
                            minimumValue = cameraCapabilities.minZoomRatio,
                            maximumValue = minOf(cameraCapabilities.maxZoomRatio, MAX_ZOOM_RATIO),
                        )
                    }
                },
        ) {
            val frameSizePx = frameSize.toPx()
            val frameTopLeft = Offset(
                x = (size.width - frameSizePx) / 2,
                y = (size.height - frameSizePx) / 2,
            )
            val frameCornerRadius = CornerRadius(24.dp.toPx())

            drawRect(color = scrimColor)
            drawRoundRect(
                color = scrimColor,
                topLeft = frameTopLeft,
                size = Size(
                    width = frameSizePx,
                    height = frameSizePx,
                ),
                cornerRadius = frameCornerRadius,
                blendMode = BlendMode.Clear,
            )
            drawRoundRect(
                color = frameColor,
                topLeft = frameTopLeft,
                size = Size(
                    width = frameSizePx,
                    height = frameSizePx,
                ),
                cornerRadius = frameCornerRadius,
                style = Stroke(width = 3.dp.toPx()),
            )
            focusPoint?.let { point ->
                drawCircle(
                    color = frameColor,
                    radius = 32.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = frameSize / 2 - 40.dp),
            visible = abs(zoomRatio - 1f) >= UNZOOMED_TOLERANCE,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ZoomRatioButton(
                modifier = Modifier.wrapContentSize(),
                zoomRatio = zoomRatio,
                onClick = { zoomRatio = 1f },
            )
        }

        SCText(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = frameSize / 2 + 32.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = hint,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textLight,
            textAlign = TextAlign.Center,
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanCameraLayer() {
    SCTheme {
        ScanCameraLayer(
            modifier = Modifier.fillMaxSize(),
            hint = stringResource(Res.string.place_code_in_frame),
            lens = CameraLens.BACK,
            isDetectionEnabled = true,
            isTorchEnabled = false,
            capabilities = null,
            onBarcodeDetected = {},
            onCapabilitiesChanged = {},
            onCameraUnavailable = {},
        )
    }
}
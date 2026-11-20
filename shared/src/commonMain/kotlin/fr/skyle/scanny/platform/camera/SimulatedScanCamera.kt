package fr.skyle.scanny.platform.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.DetectedBarcode
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val SIMULATED_READING_INTERVAL = 500.milliseconds

/**
 * Debug tool, never in a release build: stands for the camera where there is none, as on the iOS
 * simulator, and reads the codes [takeBarcodes] gives, after [firstReadingDelay] then one by one, for
 * the screens that follow a scan to be tested. Its text is not translated: only developers see it.
 */
@Composable
internal fun SimulatedScanCamera(
    isDetectionEnabled: Boolean,
    firstReadingDelay: Duration,
    takeBarcodes: () -> List<DetectedBarcode>,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCapabilitiesChanged: (CameraCapabilities) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnBarcodeDetected by rememberUpdatedState(onBarcodeDetected)

    LaunchedEffect(Unit) {
        onCapabilitiesChanged(
            CameraCapabilities(
                hasTorch = false,
                minZoomRatio = 1f,
                maxZoomRatio = 1f,
            ),
        )
    }

    LaunchedEffect(isDetectionEnabled) {
        if (isDetectionEnabled) {
            // A camera takes a moment before it reads a code
            delay(firstReadingDelay)
            takeBarcodes().forEach { barcode ->
                currentOnBarcodeDetected(barcode)
                delay(SIMULATED_READING_INTERVAL)
            }
        }
    }

    Box(modifier = modifier.background(color = SCTheme.colors.backgroundBlack)) {
        SCText(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    top = 96.dp,
                    start = 32.dp,
                    end = 32.dp,
                ),
            text = "Simulated camera",
            style = SCTheme.typography.caption,
            color = SCTheme.colors.textLight,
            textAlign = TextAlign.Center,
        )
    }
}
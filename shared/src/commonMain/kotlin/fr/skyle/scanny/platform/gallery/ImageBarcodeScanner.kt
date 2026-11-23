package fr.skyle.scanny.platform.gallery

import androidx.compose.runtime.Composable
import fr.skyle.scanny.domain.model.DetectedBarcode

sealed interface ImageScanResult {

    data class Detected(
        val barcode: DetectedBarcode,
    ) : ImageScanResult

    data object NoBarcode : ImageScanResult
}

/**
 * Returns the action that lets the user pick an image, and reports the code found in it. Nothing is
 * reported when the user cancels.
 */
@Composable
expect fun rememberImageBarcodeScanner(onResult: (ImageScanResult) -> Unit): () -> Unit
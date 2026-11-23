package fr.skyle.scanny.platform.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.platform.camera.symbologyToBarcodeFormat
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberImageBarcodeScanner(onResult: (ImageScanResult) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    val pickerDelegate = remember {
        ImagePickerDelegate { data ->
            // Called on a background thread, where Vision can work
            val result = data?.let(::scanImage) ?: ImageScanResult.NoBarcode
            dispatch_async(dispatch_get_main_queue()) {
                currentOnResult(result)
            }
        }
    }

    return remember(pickerDelegate) {
        {
            presentImagePicker(delegate = pickerDelegate)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun scanImage(data: NSData): ImageScanResult {
    val image = UIImage.imageWithData(data)?.CGImage ?: return ImageScanResult.NoBarcode
    val request = VNDetectBarcodesRequest()
    val handler = VNImageRequestHandler(
        cGImage = image,
        options = emptyMap<Any?, Any?>(),
    )
    handler.performRequests(
        requests = listOf(request),
        error = null,
    )

    val detectedBarcode = request.results
        ?.filterIsInstance<VNBarcodeObservation>()
        ?.firstNotNullOfOrNull { observation ->
            val rawValue = observation.payloadStringValue ?: return@firstNotNullOfOrNull null
            val format = symbologyToBarcodeFormat(symbology = observation.symbology) ?: return@firstNotNullOfOrNull null
            DetectedBarcode(
                rawValue = rawValue,
                format = format,
            )
        }

    return if (detectedBarcode != null) ImageScanResult.Detected(barcode = detectedBarcode) else ImageScanResult.NoBarcode
}
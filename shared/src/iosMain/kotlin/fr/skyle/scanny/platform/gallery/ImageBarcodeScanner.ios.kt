package fr.skyle.scanny.platform.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import fr.skyle.scanny.platform.camera.DetectedBarcode
import fr.skyle.scanny.platform.camera.symbologyToBarcodeFormat
import fr.skyle.scanny.platform.ui.topViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberImageBarcodeScanner(onResult: (ImageScanResult) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    val pickerDelegate = remember {
        ImagePickerDelegate { result ->
            currentOnResult(result)
        }
    }

    return remember(pickerDelegate) {
        {
            val configuration = PHPickerConfiguration().apply {
                filter = PHPickerFilter.imagesFilter
                selectionLimit = 1
            }
            val picker = PHPickerViewController(configuration = configuration).apply {
                delegate = pickerDelegate
            }
            topViewController()?.presentViewController(
                viewControllerToPresent = picker,
                animated = true,
                completion = null,
            )
        }
    }
}

private class ImagePickerDelegate(
    private val onResult: (ImageScanResult) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>,
    ) {
        picker.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )

        // No result when the user cancels
        val itemProvider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider ?: return
        itemProvider.loadDataRepresentationForTypeIdentifier(typeIdentifier = "public.image") { data, _ ->
            // Called on a background thread, where Vision can work
            val result = data?.let(::scanImage) ?: ImageScanResult.NoBarcode
            dispatch_async(dispatch_get_main_queue()) {
                onResult(result)
            }
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
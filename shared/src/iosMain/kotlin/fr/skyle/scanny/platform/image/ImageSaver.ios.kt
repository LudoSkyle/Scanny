package fr.skyle.scanny.platform.image

import fr.skyle.scanny.platform.ui.topViewController
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceCreationOptions
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class IosImageSaver : ImageSaver {

    override val canSave: Boolean = true

    // Adding only: the app asks no access to the photos already there
    override suspend fun saveImage(
        png: ByteArray,
        fileName: String,
    ): SavedImage =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
                if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) {
                    continuation.resumeWithException(IllegalStateException("The photos are not open to the app: $status"))
                    return@requestAuthorizationForAccessLevel
                }

                // The asset is known by its placeholder before it is written
                var localIdentifier: String? = null
                PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                    changeBlock = {
                        val request = PHAssetCreationRequest.creationRequestForAsset()
                        request.addResourceWithType(
                            type = PHAssetResourceTypePhoto,
                            data = png.toNSData(),
                            options = PHAssetResourceCreationOptions().apply {
                                originalFilename = fileName
                            },
                        )
                        localIdentifier = request.placeholderForCreatedAsset?.localIdentifier
                    },
                    completionHandler = { isSaved, error ->
                        if (isSaved) {
                            continuation.resume(SavedImage(id = localIdentifier.orEmpty()))
                        } else {
                            continuation.resumeWithException(IllegalStateException("The photos refused the image: ${error?.localizedDescription}"))
                        }
                    },
                )
            }
        }

    /**
     * The screen of iOS that keeps a file where the user chooses, in Files: the photos keep no SVG
     * image. Null when the user cancels.
     */
    override suspend fun saveSvg(
        svg: String,
        fileName: String,
    ): SavedImage? {
        val url = checkNotNull(NSURL.fileURLWithPath(path = NSTemporaryDirectory()).URLByAppendingPathComponent(fileName)) {
            "No temporary file for the SVG image $fileName"
        }
        check(svg.encodeToByteArray().toNSData().writeToURL(url = url, atomically = true)) {
            "Failed to write the SVG image $fileName"
        }

        return suspendCancellableCoroutine { continuation ->
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
            val delegate = ExportDelegate { savedUrl ->
                exportDelegate = null
                continuation.resume(savedUrl?.let { saved -> SavedImage(id = saved, mimeType = SavedImage.SVG_MIME_TYPE) })
            }
            exportDelegate = delegate
            picker.delegate = delegate
            topViewController()?.presentViewController(
                viewControllerToPresent = picker,
                animated = true,
                completion = null,
            ) ?: continuation.resumeWithException(IllegalStateException("No screen to show where to keep $fileName"))
        }
    }

    // Kept here while the picker shows: it only holds its delegate weakly
    private var exportDelegate: ExportDelegate? = null
}

// Gives the URL the file was kept at, or null when the user cancelled
private class ExportDelegate(
    private val onCompletion: (String?) -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        onCompletion((didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCompletion(null)
    }
}

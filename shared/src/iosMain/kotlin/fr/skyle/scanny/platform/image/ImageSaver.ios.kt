package fr.skyle.scanny.platform.image

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceCreationOptions
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
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
}

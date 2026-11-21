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
    ) {
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
                if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) {
                    continuation.resumeWithException(IllegalStateException("The photos are not open to the app: $status"))
                    return@requestAuthorizationForAccessLevel
                }

                PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                    changeBlock = {
                        PHAssetCreationRequest.creationRequestForAsset().addResourceWithType(
                            type = PHAssetResourceTypePhoto,
                            data = png.toNSData(),
                            options = PHAssetResourceCreationOptions().apply {
                                originalFilename = fileName
                            },
                        )
                    },
                    completionHandler = { isSaved, error ->
                        if (isSaved) {
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(IllegalStateException("The photos refused the image: ${error?.localizedDescription}"))
                        }
                    },
                )
            }
        }
    }
}
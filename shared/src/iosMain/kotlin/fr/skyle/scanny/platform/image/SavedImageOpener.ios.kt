package fr.skyle.scanny.platform.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// The scheme of the Photos app, the only way into it: iOS lets no app open a given photo
private const val PHOTOS_URL = "photos-redirect://"

// The scheme of the Files app, followed by the path of a file to show
private const val FILES_URL = "shareddocuments://"
private const val FILE_SCHEME = "file://"

@Composable
internal actual fun rememberSavedImageOpener(): (SavedImage) -> Boolean =
    remember {
        { savedImage ->
            // A file shows in Files, by its URL, already encoded, with the scheme of Files for its own
            val url = if (savedImage.isSvg) {
                NSURL.URLWithString(savedImage.id.replaceFirst(FILE_SCHEME, FILES_URL))
            } else {
                NSURL.URLWithString(PHOTOS_URL)
            }
            if (url != null) {
                UIApplication.sharedApplication.openURL(
                    url = url,
                    options = emptyMap<Any?, Any?>(),
                    completionHandler = null,
                )
            }
            url != null
        }
    }

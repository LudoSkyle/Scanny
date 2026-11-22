package fr.skyle.scanny.platform.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// The scheme of the Photos app, the only way into it: iOS lets no app open a given photo
private const val PHOTOS_URL = "photos-redirect://"

@Composable
internal actual fun rememberSavedImageOpener(): (SavedImage) -> Boolean =
    remember {
        { _ ->
            val url = NSURL.URLWithString(PHOTOS_URL)
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

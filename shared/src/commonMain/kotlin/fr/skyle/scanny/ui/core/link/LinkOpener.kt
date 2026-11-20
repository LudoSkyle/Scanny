package fr.skyle.scanny.ui.core.link

import androidx.compose.ui.platform.UriHandler
import co.touchlab.kermit.Logger

/**
 * Opens the app a link leads to, and tells whether the device has one. Android tells when it has none;
 * iOS never does, and opens nothing.
 */
internal fun UriHandler.openLink(link: String): Boolean =
    try {
        openUri(link)
        true
    } catch (exception: Exception) {
        // No app on the device opens this kind of link
        Logger.w(throwable = exception) { "Failed to open a link" }
        false
    }
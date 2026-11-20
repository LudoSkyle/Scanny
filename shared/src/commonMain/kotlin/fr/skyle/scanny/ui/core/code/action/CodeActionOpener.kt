package fr.skyle.scanny.ui.core.code.action

import androidx.compose.ui.platform.UriHandler
import co.touchlab.kermit.Logger
import fr.skyle.scanny.ui.core.code.model.CodeAction

/** Opens the app a link leads to, and tells whether the device has one. */
internal fun UriHandler.open(action: CodeAction.Link): Boolean =
    try {
        openUri(action.link)
        true
    } catch (exception: Exception) {
        // No app on the device opens this kind of link
        Logger.w(throwable = exception) { "Failed to open the link of a scanned code" }
        false
    }
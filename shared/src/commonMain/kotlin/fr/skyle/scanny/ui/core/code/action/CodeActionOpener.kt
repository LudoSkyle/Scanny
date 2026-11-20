package fr.skyle.scanny.ui.core.code.action

import androidx.compose.ui.platform.UriHandler
import co.touchlab.kermit.Logger
import fr.skyle.scanny.ui.core.code.model.CodeAction

/** Opens the app an action leads to, and tells whether the device has one. */
internal fun UriHandler.open(action: CodeAction): Boolean {
    val link = when (action) {
        is CodeAction.OpenLink -> action.link
        is CodeAction.WriteEmail -> action.link
        is CodeAction.WriteSms -> action.link
    }

    return try {
        openUri(link)
        true
    } catch (exception: Exception) {
        // No app on the device opens this kind of link
        Logger.w(throwable = exception) { "Failed to open the link of a scanned code" }
        false
    }
}
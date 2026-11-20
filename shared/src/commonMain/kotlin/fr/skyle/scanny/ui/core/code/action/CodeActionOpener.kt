package fr.skyle.scanny.ui.core.code.action

import androidx.compose.ui.platform.UriHandler
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.link.openLink

/** Opens the app a link of a code leads to, and tells whether the device has one. */
internal fun UriHandler.open(action: CodeAction.Link): Boolean =
    openLink(link = action.link)
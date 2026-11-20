package fr.skyle.scanny.platform.clipboard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

internal actual fun textClipEntry(text: String): ClipEntry =
    // The only way Compose gives to build a text entry on iOS, still experimental
    @OptIn(ExperimentalComposeUiApi::class)
    ClipEntry.withPlainText(text)
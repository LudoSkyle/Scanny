package fr.skyle.scanny.platform.clipboard

import androidx.compose.ui.platform.ClipEntry

/** What to give to the clipboard to copy a text: Compose builds it differently on each platform. */
internal expect fun textClipEntry(text: String): ClipEntry
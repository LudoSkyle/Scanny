package fr.skyle.scanny.platform.clipboard

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

internal actual fun textClipEntry(text: String): ClipEntry =
    ClipEntry(ClipData.newPlainText(null, text))
package fr.skyle.scanny.platform.image

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import co.touchlab.kermit.Logger

@Composable
internal actual fun rememberSavedImageOpener(): (SavedImage) -> Boolean {
    val context = LocalContext.current

    return remember(context) {
        { savedImage ->
            val intent = if (savedImage.isSvg) {
                // The downloads, where it is kept: the gallery claims an SVG image but shows it blank
                Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            } else {
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(savedImage.id.toUri(), savedImage.mimeType)
                    // The gallery reads the image the app wrote
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intent)
                true
            } catch (exception: ActivityNotFoundException) {
                Logger.e(throwable = exception) { "No app shows the image ${savedImage.id}" }
                false
            }
        }
    }
}

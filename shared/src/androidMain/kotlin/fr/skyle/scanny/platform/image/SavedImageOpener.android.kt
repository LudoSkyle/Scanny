package fr.skyle.scanny.platform.image

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
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(savedImage.id.toUri(), "image/png")
                // The gallery reads the image the app wrote
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

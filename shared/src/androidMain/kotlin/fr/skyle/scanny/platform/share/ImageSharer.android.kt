package fr.skyle.scanny.platform.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

// The folder of the cache the file provider of the app lends to the other apps
private const val SHARED_IMAGES_DIRECTORY = "shared_images"

internal class AndroidImageSharer(
    private val context: Context,
) : ImageSharer {

    override fun shareImage(
        png: ByteArray,
        fileName: String,
    ) {
        val directory = File(context.cacheDir, SHARED_IMAGES_DIRECTORY).apply { mkdirs() }
        val file = File(directory, fileName).apply { writeBytes(png) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, null)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION),
        )
    }
}
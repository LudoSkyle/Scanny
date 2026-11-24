package fr.skyle.scanny.platform.image

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// The folder of the pictures, and of the downloads, where Scanny keeps its images
private const val SCANNY_DIRECTORY = "Scanny"

internal class AndroidImageSaver(
    private val context: Context,
    sdkInt: Int,
) : ImageSaver {

    // Before Android 10, writing in the pictures needs a permission the app does not ask: the share
    // sheet keeps an image there instead
    override val canSave: Boolean = sdkInt >= Build.VERSION_CODES.Q

    override suspend fun saveImage(
        png: ByteArray,
        fileName: String,
    ): SavedImage {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { "The pictures need a permission before Android 10" }

        return saveFile(
            bytes = png,
            fileName = fileName,
            mimeType = SavedImage.PNG_MIME_TYPE,
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            directory = Environment.DIRECTORY_PICTURES,
        )
    }

    // In the downloads: the pictures take no SVG image
    override suspend fun saveSvg(
        svg: String,
        fileName: String,
    ): SavedImage {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { "The downloads need a permission before Android 10" }

        return saveFile(
            bytes = svg.encodeToByteArray(),
            fileName = fileName,
            mimeType = SavedImage.SVG_MIME_TYPE,
            collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            directory = Environment.DIRECTORY_DOWNLOADS,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        collection: Uri,
        directory: String,
    ): SavedImage =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$directory/$SCANNY_DIRECTORY")
                // Hidden from the other apps until written
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = checkNotNull(resolver.insert(collection, values)) {
                "The device refused the image $fileName"
            }
            try {
                checkNotNull(resolver.openOutputStream(uri)) { "The image $fileName could not be written" }.use { stream ->
                    stream.write(bytes)
                }
                resolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null,
                )
                SavedImage(
                    id = uri.toString(),
                    mimeType = mimeType,
                )
            } catch (exception: Exception) {
                // No half written image left on the device
                resolver.delete(uri, null, null)
                throw exception
            }
        }
}

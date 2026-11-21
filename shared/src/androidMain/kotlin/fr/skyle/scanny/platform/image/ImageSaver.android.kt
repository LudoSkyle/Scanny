package fr.skyle.scanny.platform.image

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// The folder of the pictures where Scanny keeps its images
private const val PICTURES_DIRECTORY = "Scanny"

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
    ) {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { "The pictures need a permission before Android 10" }

        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$PICTURES_DIRECTORY")
                // Hidden from the other apps until written
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)) {
                "The pictures refused the image $fileName"
            }
            try {
                checkNotNull(resolver.openOutputStream(uri)) { "The image $fileName could not be written" }.use { stream ->
                    stream.write(png)
                }
                resolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null,
                )
            } catch (exception: Exception) {
                // No half written image left in the pictures
                resolver.delete(uri, null, null)
                throw exception
            }
        }
    }
}
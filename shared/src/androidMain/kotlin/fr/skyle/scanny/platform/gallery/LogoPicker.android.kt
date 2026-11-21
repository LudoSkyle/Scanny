package fr.skyle.scanny.platform.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

@Composable
actual fun rememberLogoPicker(onResult: (LogoPickResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                currentOnResult(
                    readLogo(
                        context = context,
                        uri = uri,
                    ),
                )
            }
        }
    }

    return remember(launcher) {
        {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}

private suspend fun readLogo(
    context: Context,
    uri: Uri,
): LogoPickResult =
    withContext(Dispatchers.IO) {
        try {
            val bytes = checkNotNull(context.contentResolver.openInputStream(uri)) { "No content for $uri" }
                .use { stream -> stream.readBytes() }
            LogoPickResult.Picked(png = bytes.toLogoPng())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Logger.w(throwable = exception) { "Failed to read the image $uri" }
            LogoPickResult.Unreadable
        }
    }

/** An image, upright and reduced to the size of a logo, as a PNG. Throws for bytes that hold no image. */
internal fun ByteArray.toLogoPng(): ByteArray {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // Turned upright as its EXIF says, and a HEIF photo read
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(this))) { decoder, info, _ ->
            val (width, height) = logoSize(
                width = info.size.width,
                height = info.size.height,
            )
            decoder.setTargetSize(width, height)
            // In memory rather than on the graphics card: compressed next
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        decodeScaledBitmap()
    }

    return ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }
}

// Before Android 9: read at a fraction of its size first, a photo being too large to be read whole,
// then reduced. Its EXIF is not read: a photo can stay on its side
private fun ByteArray.decodeScaledBitmap(): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    check(bounds.outWidth > 0 && bounds.outHeight > 0) { "The bytes hold no image" }

    val options = BitmapFactory.Options().apply {
        inSampleSize = Integer.highestOneBit(maxOf(1, maxOf(bounds.outWidth, bounds.outHeight) / LOGO_MAX_SIDE))
    }
    val decoded = checkNotNull(BitmapFactory.decodeByteArray(this, 0, size, options)) { "The bytes hold no image" }
    val (width, height) = logoSize(
        width = decoded.width,
        height = decoded.height,
    )
    return Bitmap.createScaledBitmap(decoded, width, height, true)
}

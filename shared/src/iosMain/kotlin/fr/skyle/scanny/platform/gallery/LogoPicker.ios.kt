package fr.skyle.scanny.platform.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import fr.skyle.scanny.platform.image.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.math.roundToInt

@Composable
actual fun rememberLogoPicker(onResult: (LogoPickResult) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    val pickerDelegate = remember {
        ImagePickerDelegate { data ->
            // Called on a background thread: the image is reduced there
            val result = data?.toLogoPng()?.let(LogoPickResult::Picked) ?: LogoPickResult.Unreadable
            dispatch_async(dispatch_get_main_queue()) {
                currentOnResult(result)
            }
        }
    }

    return remember(pickerDelegate) {
        {
            presentImagePicker(delegate = pickerDelegate)
        }
    }
}

/**
 * An image, upright and reduced to the size of a logo, as a PNG; null for data that holds no image.
 * Drawn again, a HEIF photo turned upright as it is shown.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toLogoPng(): ByteArray? {
    val image = UIImage.imageWithData(this) ?: return null
    // The size of the image as it is shown, upright
    val (width, height) = image.size.useContents {
        logoSize(
            width = width.roundToInt(),
            height = height.roundToInt(),
        )
    }
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
        // In pixels, not in points of the screen
        scale = 1.0
    }
    val renderer = UIGraphicsImageRenderer(
        size = CGSizeMake(width = width.toDouble(), height = height.toDouble()),
        format = format,
    )
    return renderer.PNGDataWithActions { _ ->
        image.drawInRect(CGRectMake(x = 0.0, y = 0.0, width = width.toDouble(), height = height.toDouble()))
    }.toByteArray()
}

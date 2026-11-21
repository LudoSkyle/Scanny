package fr.skyle.scanny.platform.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import co.touchlab.kermit.Logger
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import kotlin.math.min

// The share of the shorter side of the image kept around its center: the frame of the screen, with a margin
private const val CENTER_SHARE = 0.5f

// A grey with no color: the codes are read in shades of grey only
private const val NEUTRAL_CHROMA: Byte = -128

/**
 * ML Kit reduces an image before looking for codes in it: a code far away, small in the image, is
 * lost however fine the image is. One image out of two, only its center goes to ML Kit, which then
 * reads a far code as if the camera zoomed on it, without the preview zooming. The other images go
 * whole, for a code held close, wider than the center.
 */
internal class BarcodeAnalyzer(
    private val barcodeScanner: BarcodeScanner,
    private val resultExecutor: Executor,
    private val onBarcodesRead: (List<Barcode>) -> Unit,
) : ImageAnalysis.Analyzer {

    private var isCenterNext = false

    // Reused from an image to the next: the image is closed, and the next one comes, once ML Kit is done
    private var centerBytes: ByteArray? = null

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val isCenter = isCenterNext
        isCenterNext = !isCenterNext
        val task = try {
            val inputImage = if (isCenter) {
                image.centerInputImage()
            } else {
                InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            }
            barcodeScanner.process(inputImage)
        } catch (exception: RuntimeException) {
            // An image left open would stop the camera from sending the next ones
            Logger.w(throwable = exception) { "Failed to read the codes of an image" }
            image.close()
            return
        }

        task
            .addOnSuccessListener(resultExecutor) { barcodes ->
                onBarcodesRead(barcodes)
            }
            .addOnFailureListener { exception ->
                Logger.w(throwable = exception) { "Failed to read the codes of an image" }
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    // The center of the image in NV21, its luminance only: what ML Kit needs to read a code
    private fun ImageProxy.centerInputImage(): InputImage {
        val crop = centerCrop(
            width = width,
            height = height,
        )
        val lumaSize = crop.side * crop.side

        val bytes = centerBytes?.takeIf { bytes -> bytes.size == lumaSize * 3 / 2 }
            ?: ByteArray(lumaSize * 3 / 2).also { bytes ->
                bytes.fill(NEUTRAL_CHROMA, lumaSize, bytes.size)
                centerBytes = bytes
            }

        // The luminance comes first in YUV_420_888, one byte per pixel
        val lumaPlane = planes[0]
        copyCenterLuma(
            luma = lumaPlane.buffer,
            rowStride = lumaPlane.rowStride,
            crop = crop,
            target = bytes,
        )

        return InputImage.fromByteArray(
            bytes,
            crop.side,
            crop.side,
            imageInfo.rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21,
        )
    }
}

/** Where the center of an image is cut out: a square, from its top left corner, in pixels. */
internal data class CenterCrop(
    val left: Int,
    val top: Int,
    val side: Int,
)

// Even sizes and positions, as NV21 shares a color between two by two pixels
internal fun centerCrop(
    width: Int,
    height: Int,
): CenterCrop {
    val side = (min(width, height) * CENTER_SHARE).toInt() and 1.inv()
    return CenterCrop(
        left = ((width - side) / 2) and 1.inv(),
        top = ((height - side) / 2) and 1.inv(),
        side = side,
    )
}

// A row of the image takes rowStride bytes, more than its width: the center is copied row by row
internal fun copyCenterLuma(
    luma: ByteBuffer,
    rowStride: Int,
    crop: CenterCrop,
    target: ByteArray,
) {
    for (row in 0 until crop.side) {
        luma.position((crop.top + row) * rowStride + crop.left)
        luma.get(target, row * crop.side, crop.side)
    }
}
package fr.skyle.scanny.ui.core.code.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import io.github.alexzhirkevich.qrose.ImageFormat
import io.github.alexzhirkevich.qrose.QrCodePainter
import io.github.alexzhirkevich.qrose.oned.BarcodePainter
import io.github.alexzhirkevich.qrose.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Draws a code into an image, for another app to show it or keep it. */
interface CodeImageRenderer {

    /**
     * The code holding [rawValue] in [format], as a PNG, drawn away from the main thread: an image of a
     * thousand pixels a side. Throws for a text its format refuses.
     */
    suspend fun renderPng(
        rawValue: String,
        format: BarcodeFormat,
    ): ByteArray
}

// Large enough to be printed. A scanner needs a blank margin around a code: the quiet zone
private const val QR_CODE_SIZE = 1024
private const val BARCODE_WIDTH = 1200
private const val BARCODE_HEIGHT = 480
private const val MARGIN = 64f

internal class QroseCodeImageRenderer : CodeImageRenderer {

    override suspend fun renderPng(
        rawValue: String,
        format: BarcodeFormat,
    ): ByteArray =
        withContext(Dispatchers.Default) {
            draw(
                rawValue = rawValue,
                format = format,
            )
        }

    private fun draw(
        rawValue: String,
        format: BarcodeFormat,
    ): ByteArray {
        val barcodeType = format.toBarcodeType()
        val painter: Painter = if (barcodeType != null) {
            BarcodePainter(
                data = rawValue,
                type = barcodeType,
            )
        } else {
            QrCodePainter(data = rawValue)
        }
        val width = if (barcodeType != null) BARCODE_WIDTH else QR_CODE_SIZE
        val height = if (barcodeType != null) BARCODE_HEIGHT else QR_CODE_SIZE

        // Dark on white whatever the theme, as on the screen
        val bitmap = ImageBitmap(width = width, height = height)
        CanvasDrawScope().draw(
            density = Density(density = 1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(image = bitmap),
            size = Size(width = width.toFloat(), height = height.toFloat()),
        ) {
            drawRect(color = Color.White)
            inset(inset = MARGIN) {
                with(painter) {
                    draw(size = size)
                }
            }
        }
        return bitmap.toByteArray(format = ImageFormat.PNG)
    }
}
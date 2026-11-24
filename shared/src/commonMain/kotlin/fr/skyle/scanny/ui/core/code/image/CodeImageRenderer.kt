package fr.skyle.scanny.ui.core.code.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.mapper.backgroundColor
import fr.skyle.scanny.ui.core.code.mapper.dotColor
import fr.skyle.scanny.ui.core.code.mapper.svgGradient
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.core.code.mapper.toQrOptions
import io.github.alexzhirkevich.qrose.CachedPainter
import io.github.alexzhirkevich.qrose.ImageFormat
import io.github.alexzhirkevich.qrose.QrCodePainter
import io.github.alexzhirkevich.qrose.oned.BarcodePainter
import io.github.alexzhirkevich.qrose.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** Draws a code into an image, for another app to show it or keep it. */
interface CodeImageRenderer {

    /**
     * The code holding [rawValue] in [format], as a PNG, drawn away from the main thread: an image of a
     * thousand pixels a side or so, a QR code in its [style]. Throws for a text its format refuses.
     */
    suspend fun renderPng(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): ByteArray

    /**
     * The same code as [renderPng], as an SVG image: its shapes, colors and gradient as vectors, which
     * stay sharp at any size, a logo as the image it is. Throws for a text its format refuses.
     */
    suspend fun renderSvg(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): String
}

// Large enough to be printed. A scanner needs a blank margin around a code: the quiet zone
private const val QR_CODE_SIZE = 1024
private const val BARCODE_WIDTH = 1200
private const val BARCODE_HEIGHT = 480
private const val MARGIN = 64f
private const val GRADIENT_ID = "gradient"

internal class QroseCodeImageRenderer : CodeImageRenderer {

    override suspend fun renderPng(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): ByteArray =
        withContext(Dispatchers.Default) {
            draw(
                rawValue = rawValue,
                format = format,
                style = style,
            )
        }

    override suspend fun renderSvg(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): String =
        withContext(Dispatchers.Default) {
            drawSvg(
                rawValue = rawValue,
                format = format,
                style = style,
            )
        }

    private class Drawing(
        val painter: Painter,
        val style: CodeStyle,
        val width: Int,
        val height: Int,
    )

    private fun prepare(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): Drawing {
        val barcodeType = format.toBarcodeType()
        val isQrCode = format == BarcodeFormat.QR_CODE
        // Only a QR code is customized, as on the screen
        val drawnStyle = if (isQrCode) style else CodeStyle()
        val painter: Painter = when {
            barcodeType != null -> BarcodePainter(
                data = rawValue,
                type = barcodeType,
            )

            isQrCode -> QrCodePainter(
                data = rawValue,
                options = drawnStyle.toQrOptions(),
            )

            else -> MatrixCodePainter(
                rawValue = rawValue,
                format = format,
                color = drawnStyle.dotColor(),
            )
        }
        val (width, height) = when {
            barcodeType != null -> BARCODE_WIDTH to BARCODE_HEIGHT
            isQrCode -> QR_CODE_SIZE to QR_CODE_SIZE
            else -> painter.imageSize()
        }
        return Drawing(
            painter = painter,
            style = drawnStyle,
            width = width,
            height = height,
        )
    }

    private fun draw(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): ByteArray {
        val drawing = prepare(
            rawValue = rawValue,
            format = format,
            style = style,
        )

        val bitmap = ImageBitmap(width = drawing.width, height = drawing.height)
        CanvasDrawScope().draw(
            density = Density(density = 1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(image = bitmap),
            size = Size(width = drawing.width.toFloat(), height = drawing.height.toFloat()),
        ) {
            // The margin too, for the quiet zone to be of the color of the background
            drawRect(color = drawing.style.backgroundColor())
            inset(inset = MARGIN) {
                with(drawing.painter) {
                    draw(size = size)
                }
            }
        }
        return bitmap.toByteArray(format = ImageFormat.PNG)
    }

    private fun drawSvg(
        rawValue: String,
        format: BarcodeFormat,
        style: CodeStyle,
    ): String {
        val drawing = prepare(
            rawValue = rawValue,
            format = format,
            style = style,
        )
        // A QR code is square: qrose draws it from the top left corner of the margin, the size of its side
        val gradient = drawing.style.svgGradient(
            id = GRADIENT_ID,
            left = MARGIN,
            top = MARGIN,
            size = drawing.width - 2 * MARGIN,
        ).takeIf { format == BarcodeFormat.QR_CODE }

        val canvas = SvgCanvas(shaderFill = gradient?.let { "url(#$GRADIENT_ID)" })
        CanvasDrawScope().draw(
            density = Density(density = 1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(width = drawing.width.toFloat(), height = drawing.height.toFloat()),
        ) {
            inset(inset = MARGIN) {
                val painter = drawing.painter
                // A painter of qrose draws into an image it keeps, then draws the image: its drawing
                // itself, public, is what keeps the shapes as vectors
                if (painter is CachedPainter) {
                    with(painter) {
                        onCache()
                    }
                } else {
                    with(painter) {
                        draw(size = size)
                    }
                }
            }
        }

        val background = drawing.style.backgroundColor()
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"").append(drawing.width)
                .append("\" height=\"").append(drawing.height).append("\" viewBox=\"0 0 ")
                .append(drawing.width).append(' ').append(drawing.height).append("\">")
            gradient?.let { definition -> append("<defs>").append(definition).append("</defs>") }
            // The margin too, for the quiet zone to be of the color of the background
            append("<rect width=\"").append(drawing.width).append("\" height=\"").append(drawing.height)
                .append("\" fill=\"").append(background.toSvgColor()).append('"')
            if (background.alpha < 1f) {
                append(" fill-opacity=\"").append(format(background.alpha)).append('"')
            }
            append("/>")
            append(canvas.content())
            append("</svg>\n")
        }
    }

    // In the proportions of the code's modules, its longer side as long as a QR code's, or a barcode's
    // for a wide one
    private fun Painter.imageSize(): Pair<Int, Int> {
        val aspectRatio = intrinsicSize.width / intrinsicSize.height
        return if (aspectRatio > 1f) {
            val codeWidth = BARCODE_WIDTH - 2 * MARGIN
            BARCODE_WIDTH to (codeWidth / aspectRatio + 2 * MARGIN).roundToInt()
        } else {
            val codeHeight = QR_CODE_SIZE - 2 * MARGIN
            (codeHeight * aspectRatio + 2 * MARGIN).roundToInt() to QR_CODE_SIZE
        }
    }
}
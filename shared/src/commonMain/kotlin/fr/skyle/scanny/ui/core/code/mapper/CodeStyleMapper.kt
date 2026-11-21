package fr.skyle.scanny.ui.core.code.mapper

import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.options.square

// Of the side of a corner: rounded, still far from a circle
private const val CORNER_RADIUS = 0.25f

/**
 * The options qrose draws a QR code with in this style. qrose compares the shapes by identity: they
 * are built once per style, not at each drawing.
 */
internal fun CodeStyle.toQrOptions(): QrOptions =
    QrOptions(
        shapes = QrShapes(
            darkPixel = dotShape.toPixelShape(),
            ball = cornerShape.toBallShape(),
            frame = cornerShape.toFrameShape(),
        ),
        colors = QrColors(dark = QrBrush.solid(color = dotColor())),
    )

/** The color of the dots of a code in this style. */
internal fun CodeStyle.dotColor(): Color =
    Color(color = darkColor)

/** The color of the background of a code in this style, around it too: its quiet zone. */
internal fun CodeStyle.backgroundColor(): Color =
    Color(color = lightColor)

private fun CodeShape.toPixelShape(): QrPixelShape =
    when (this) {
        CodeShape.SQUARE -> QrPixelShape.square()
        // Joined to their neighbors, as a stroke
        CodeShape.ROUNDED -> QrPixelShape.roundCorners()
        CodeShape.CIRCLE -> QrPixelShape.circle()
    }

private fun CodeShape.toBallShape(): QrBallShape =
    when (this) {
        CodeShape.SQUARE -> QrBallShape.square()
        CodeShape.ROUNDED -> QrBallShape.roundCorners(radius = CORNER_RADIUS)
        CodeShape.CIRCLE -> QrBallShape.circle()
    }

private fun CodeShape.toFrameShape(): QrFrameShape =
    when (this) {
        CodeShape.SQUARE -> QrFrameShape.square()
        CodeShape.ROUNDED -> QrFrameShape.roundCorners(corner = CORNER_RADIUS)
        CodeShape.CIRCLE -> QrFrameShape.circle()
    }

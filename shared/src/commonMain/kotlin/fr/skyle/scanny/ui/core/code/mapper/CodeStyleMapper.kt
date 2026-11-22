package fr.skyle.scanny.ui.core.code.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.model.CodeReadability
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogo
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.options.square
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.math.pow

// The contrast the WCAG asks of a text: a scanner reads a code as well
private const val MIN_CONTRAST_RATIO = 4.5

// Of the side of a corner: rounded, still far from a circle
private const val CORNER_RADIUS = 0.25f

// Of the side of the code: the logo and the blank around it hide less than the highest correction
// makes up for
private const val LOGO_SIZE = 0.2f
private const val LOGO_PADDING = 0.1f

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
        logo = toQrLogo(),
        // Set, not left to qrose: it keeps the lowest correction for a logo drawn with no blank around
        errorCorrectionLevel = qrErrorCorrection().toQrErrorCorrectionLevel(),
    )

/**
 * The correction a QR code in this style is encoded with: the one chosen, the lowest when left to
 * Scanny, and always the highest under a logo, which hides a part of it.
 */
internal fun CodeStyle.qrErrorCorrection(): QrErrorCorrection =
    if (logo != null) {
        QrErrorCorrection.H
    } else {
        when (errorCorrection) {
            CodeErrorCorrection.AUTO,
            CodeErrorCorrection.LOW,
            -> QrErrorCorrection.L

            CodeErrorCorrection.MEDIUM -> QrErrorCorrection.M
            CodeErrorCorrection.QUARTILE -> QrErrorCorrection.Q
            CodeErrorCorrection.HIGH -> QrErrorCorrection.H
        }
    }

/** Whether a logo can go on a code with this correction: it needs the highest, chosen or left to Scanny. */
internal val CodeErrorCorrection.allowsLogo: Boolean
    get() = this == CodeErrorCorrection.AUTO || this == CodeErrorCorrection.HIGH

/** The color of the dots of a code in this style. */
internal fun CodeStyle.dotColor(): Color =
    Color(color = darkColor)

/** The color of the background of a code in this style, around it too: its quiet zone. */
internal fun CodeStyle.backgroundColor(): Color =
    Color(color = lightColor)

// The dots under the logo and around it cleared, for the background to show there
/** How well a scanner reads a code in this style: its dots darker than its background, and enough apart. */
internal fun CodeStyle.readability(): CodeReadability =
    when {
        relativeLuminance(darkColor) > relativeLuminance(lightColor) -> CodeReadability.INVERTED
        contrastRatio(darkColor, lightColor) < MIN_CONTRAST_RATIO -> CodeReadability.LOW_CONTRAST
        else -> CodeReadability.GOOD
    }

/** The contrast ratio of the WCAG between two ARGB colors, from 1 for the same color to 21 for black and white. */
internal fun contrastRatio(
    firstArgb: Long,
    secondArgb: Long,
): Double {
    val firstLuminance = relativeLuminance(firstArgb)
    val secondLuminance = relativeLuminance(secondArgb)
    return (maxOf(firstLuminance, secondLuminance) + 0.05) / (minOf(firstLuminance, secondLuminance) + 0.05)
}

// The relative luminance of the WCAG: how light a color looks, from 0 for black to 1 for white
private fun relativeLuminance(argb: Long): Double {
    fun channel(shift: Int): Double {
        val value = ((argb shr shift) and 0xFF) / 255.0
        return if (value <= 0.03928) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(shift = 16) + 0.7152 * channel(shift = 8) + 0.0722 * channel(shift = 0)
}

private fun CodeStyle.toQrLogo(): QrLogo {
    val png = logo ?: return QrLogo()
    val image = try {
        png.decodeToImageBitmap()
    } catch (exception: Exception) {
        // A logo that cannot be read is left out, the code still drawn
        Logger.w(throwable = exception) { "Failed to read the logo of a code" }
        return QrLogo()
    }
    return QrLogo(
        painter = BitmapPainter(image = image),
        size = LOGO_SIZE,
        padding = QrLogoPadding.Accurate(size = LOGO_PADDING),
    )
}

private fun QrErrorCorrection.toQrErrorCorrectionLevel(): QrErrorCorrectionLevel =
    when (this) {
        QrErrorCorrection.L -> QrErrorCorrectionLevel.Low
        QrErrorCorrection.M -> QrErrorCorrectionLevel.Medium
        QrErrorCorrection.Q -> QrErrorCorrectionLevel.MediumHigh
        QrErrorCorrection.H -> QrErrorCorrectionLevel.High
    }

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

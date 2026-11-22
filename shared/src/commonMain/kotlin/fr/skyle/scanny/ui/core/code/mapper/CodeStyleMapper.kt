package fr.skyle.scanny.ui.core.code.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.image.ShapedLogoPainter
import fr.skyle.scanny.ui.core.code.model.CodeReadability
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogo
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.options.square
import org.jetbrains.compose.resources.decodeToImageBitmap

// Of the side of a corner: rounded, still far from a circle
private const val CORNER_RADIUS = 0.25f

// Half the diagonal of a square of side 1: a radial gradient reaches its second color in the corners
private const val HALF_DIAGONAL = 0.7071f

// Of the side of the code: the logo and the blank around it hide less than the highest correction
// makes up for
private const val LOGO_SIZE = 0.2f
private const val LOGO_NORMAL_MARGIN = 0.1f
private const val LOGO_LARGE_MARGIN = 0.25f

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
        colors = QrColors(
            dark = dotBrush(),
            // Left unspecified, the corners are painted with the dots, their gradient included
            ball = cornerBrush(),
            frame = cornerBrush(),
        ),
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

/** The color of the three corners of a code in this style: their own, or the one of the dots. */
internal fun CodeStyle.cornerColor(): Color =
    Color(color = cornerColor ?: darkColor)

/** The color of the background of a code in this style, around it too: its quiet zone. */
internal fun CodeStyle.backgroundColor(): Color =
    Color(color = lightColor)

/**
 * How well a scanner reads a code in this style: its dots, its corners and the second color of its
 * gradient darker than its background, and less than half as light. The binarizer of ZXing, the reader
 * of most scanning apps on Android, takes a uniform light area for the background and sets its
 * threshold at half of it: a dot as light as that half reads as background. The rule tells exactly which
 * colors ZXing reads (CodeImageReadabilityTest), where the 4.5:1 the WCAG asks of a text warned about
 * many it reads, a vivid red on white for one.
 */
internal fun CodeStyle.readability(): CodeReadability {
    val colors = listOfNotNull(
        darkColor,
        cornerColor,
        gradientColor.takeIf { gradient != CodeGradient.NONE },
    )
    val lightLuminance = scannerLuminance(lightColor)
    return when {
        colors.any { argb -> scannerLuminance(argb) > lightLuminance } -> CodeReadability.INVERTED
        colors.any { argb -> scannerLuminance(argb) >= lightLuminance / 2 } -> CodeReadability.LOW_CONTRAST
        else -> CodeReadability.GOOD
    }
}

/**
 * The luminance ZXing reads in an image, from 0 for black to 255 for white, in whole numbers as it
 * counts it: its green counts twice.
 */
internal fun scannerLuminance(argb: Long): Int =
    ((((argb shr 16) and 0xFF) + 2 * ((argb shr 8) and 0xFF) + (argb and 0xFF)) / 4).toInt()

private fun CodeStyle.dotBrush(): QrBrush {
    val colors = listOf(Color(color = darkColor), Color(color = gradientColor))
    return when (gradient) {
        CodeGradient.NONE -> QrBrush.solid(color = dotColor())

        CodeGradient.LINEAR -> QrBrush.brush { size ->
            Brush.linearGradient(
                colors = colors,
                start = Offset.Zero,
                end = Offset(x = size, y = size),
            )
        }

        CodeGradient.RADIAL -> QrBrush.brush { size ->
            Brush.radialGradient(
                colors = colors,
                center = Offset(x = size / 2, y = size / 2),
                radius = size * HALF_DIAGONAL,
            )
        }
    }
}

private fun CodeStyle.cornerBrush(): QrBrush =
    cornerColor?.let { argb -> QrBrush.solid(color = Color(color = argb)) } ?: QrBrush.Unspecified

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
        painter = ShapedLogoPainter(
            image = image,
            shape = logoShape,
            cornerRadius = CORNER_RADIUS,
        ),
        size = LOGO_SIZE,
        // The dots under the logo and around it cleared, in its shape
        padding = when (logoMargin) {
            CodeLogoMargin.NONE -> QrLogoPadding.Empty
            CodeLogoMargin.NORMAL -> QrLogoPadding.Accurate(size = LOGO_NORMAL_MARGIN)
            CodeLogoMargin.LARGE -> QrLogoPadding.Accurate(size = LOGO_LARGE_MARGIN)
        },
        shape = logoShape.toLogoShape(),
    )
}

private fun CodeShape.toLogoShape(): QrLogoShape =
    when (this) {
        CodeShape.SQUARE -> QrLogoShape.Default
        CodeShape.ROUNDED -> QrLogoShape.roundCorners(radius = CORNER_RADIUS)
        CodeShape.CIRCLE -> QrLogoShape.circle()
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

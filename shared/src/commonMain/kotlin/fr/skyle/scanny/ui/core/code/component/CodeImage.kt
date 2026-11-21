package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_not_drawable
import fr.skyle.scanny.ui.core.code.mapper.backgroundColor
import fr.skyle.scanny.ui.core.code.mapper.canHold
import fr.skyle.scanny.ui.core.code.mapper.qrErrorCorrection
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.core.code.mapper.toQrOptions
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import io.github.alexzhirkevich.qrose.oned.rememberBarcodePainter
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource

// A barcode is wider than high, a QR code square
private const val BARCODE_ASPECT_RATIO = 2.5f

/**
 * A code drawn again from its text, dark on white whatever the theme, for any scanner to read it, or
 * in the [style] the user chose for a QR code. A text its format refuses, a barcode with a wrong check
 * digit for instance, is said instead.
 */
@Composable
fun CodeImage(
    rawValue: String,
    format: BarcodeFormat,
    contentDescription: String,
    modifier: Modifier = Modifier,
    style: CodeStyle = CodeStyle(),
) {
    val barcodeType = format.toBarcodeType()
    // A barcode stays dark on white
    val drawnStyle = if (barcodeType != null) CodeStyle() else style

    // Tried before drawing, with the encoder and the correction that draw it: a QR code too dense for
    // qrose would fail while drawn, with no way to catch it
    val errorCorrection = drawnStyle.qrErrorCorrection()
    val canHold = remember(rawValue, format, errorCorrection) {
        format.canHold(
            rawValue = rawValue,
            errorCorrection = errorCorrection,
        )
    }
    if (!canHold) {
        SCText(
            modifier = modifier,
            text = stringResource(Res.string.code_not_drawable),
            style = SCTheme.typography.body2,
            color = SCTheme.colors.text,
        )
        return
    }

    Image(
        modifier = modifier
            .background(color = drawnStyle.backgroundColor())
            .padding(all = 16.dp)
            .aspectRatio(if (barcodeType != null) BARCODE_ASPECT_RATIO else 1f),
        painter = if (barcodeType != null) {
            rememberBarcodePainter(
                data = rawValue,
                type = barcodeType,
            )
        } else {
            rememberQrCodePainter(
                data = rawValue,
                options = remember(drawnStyle) { drawnStyle.toQrOptions() },
            )
        },
        contentDescription = contentDescription,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeImage() {
    SCTheme {
        CodeImage(
            modifier = Modifier.fillMaxWidth(),
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            contentDescription = "QR code",
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeImageWithStyle() {
    SCTheme {
        CodeImage(
            modifier = Modifier.fillMaxWidth(),
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            contentDescription = "QR code",
            style = CodeStyle(
                darkColor = 0xFF283593,
                lightColor = 0xFFFFF8E1,
                dotShape = CodeShape.CIRCLE,
                cornerShape = CodeShape.ROUNDED,
            ),
        )
    }
}
package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_not_drawable
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import io.github.alexzhirkevich.qrose.oned.rememberBarcodePainter
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource

// A barcode is wider than high, a QR code square
private const val BARCODE_ASPECT_RATIO = 2.5f

/**
 * A code drawn again from its text, dark on white whatever the theme, for any scanner to read it. A
 * text its format refuses, a barcode with a wrong check digit for instance, is said instead.
 */
@Composable
fun CodeImage(
    rawValue: String,
    format: BarcodeFormat,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val barcodeType = format.toBarcodeType()
    val painter = if (barcodeType != null) {
        rememberBarcodePainter(
            data = rawValue,
            type = barcodeType,
            onError = { InvalidCodePainter },
        )
    } else {
        rememberQrCodePainter(data = rawValue)
    }

    if (painter === InvalidCodePainter) {
        SCText(
            modifier = modifier,
            text = stringResource(Res.string.code_not_drawable),
            style = SCTheme.typography.body2,
            color = SCTheme.colors.text,
        )
    } else {
        Image(
            modifier = modifier
                .background(color = Color.White)
                .padding(all = 16.dp)
                .aspectRatio(if (barcodeType != null) BARCODE_ASPECT_RATIO else 1f),
            painter = painter,
            contentDescription = contentDescription,
        )
    }
}

// Given by the barcode painter for a text its format refuses
private object InvalidCodePainter : Painter() {

    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() = Unit
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
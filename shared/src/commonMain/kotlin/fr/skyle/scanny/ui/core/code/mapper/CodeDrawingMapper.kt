package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.encoder.aztec.AztecEncoder
import fr.skyle.scanny.ui.core.code.encoder.datamatrix.DataMatrixEncoder
import io.github.alexzhirkevich.qrose.QroseEncoders
import io.github.alexzhirkevich.qrose.matrix.MatrixCodeEncoder
import io.github.alexzhirkevich.qrose.matrix.PDF417
import io.github.alexzhirkevich.qrose.matrix.QR
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.oned.BarcodeType

// A row of a PDF417 is several modules high, as with ZXing: one module high, a scanner loses its rows
private const val PDF417_ROW_HEIGHT = 4

// The last character of ISO 8859-1, the charset scanners read a Data Matrix and an Aztec code in
private const val LATIN_1_MAX = 255

/** The type of barcode qrose draws for this format, null for a code in two dimensions. */
internal fun BarcodeFormat.toBarcodeType(): BarcodeType? =
    when (this) {
        BarcodeFormat.EAN_13 -> BarcodeType.EAN13
        BarcodeFormat.EAN_8 -> BarcodeType.EAN8
        BarcodeFormat.UPC_A -> BarcodeType.UPCA
        BarcodeFormat.UPC_E -> BarcodeType.UPCE
        BarcodeFormat.CODE_39 -> BarcodeType.Code39
        BarcodeFormat.CODE_93 -> BarcodeType.Code93
        BarcodeFormat.CODE_128 -> BarcodeType.Code128
        BarcodeFormat.ITF -> BarcodeType.ITF
        BarcodeFormat.CODABAR -> BarcodeType.Codabar
        // The codes in two dimensions
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF417,
        -> null
    }

/**
 * The encoder of a code in two dimensions that qrose does not draw, null for a QR code, which qrose
 * draws in its style, or for a barcode. qrose's encoders of Data Matrix and Aztec write wrong codes:
 * Scanny has corrected copies of them, in `encoder`.
 */
internal fun BarcodeFormat.toMatrixEncoder(): MatrixCodeEncoder? =
    when (this) {
        BarcodeFormat.DATA_MATRIX -> DataMatrixEncoder()
        BarcodeFormat.AZTEC -> AztecEncoder()
        BarcodeFormat.PDF417 -> QroseEncoders.PDF417()
        BarcodeFormat.QR_CODE,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128,
        BarcodeFormat.ITF,
        BarcodeFormat.CODABAR,
        -> null
    }

/** How many modules high a row of a code of this format is drawn, by [toMatrixEncoder]'s matrix. */
internal val BarcodeFormat.matrixRowHeight: Int
    get() = if (this == BarcodeFormat.PDF417) PDF417_ROW_HEIGHT else 1

/**
 * Whether a code of this format can hold every character of [rawValue]: a Data Matrix and an Aztec
 * code hold ISO 8859-1 only, which scanners read them in. A barcode's characters are tried by its
 * encoder, with its length and its check digit.
 */
internal fun BarcodeFormat.holdsCharactersOf(rawValue: String): Boolean =
    when (this) {
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        -> rawValue.all { c -> c.code <= LATIN_1_MAX }

        else -> true
    }

/**
 * Whether a code of this format can hold [rawValue], tried with the encoder that draws it: a barcode
 * refuses a wrong length or a wrong check digit, a code in two dimensions a text too long for it, a QR
 * code for [errorCorrection].
 * A QR code that may get a logo is tried with the highest correction, the one a logo can ask for.
 */
internal fun BarcodeFormat.canHold(
    rawValue: String,
    errorCorrection: QrErrorCorrection = QrErrorCorrection.L,
): Boolean {
    if (rawValue.isEmpty() || !holdsCharactersOf(rawValue)) {
        return false
    }

    return runCatching {
        val barcodeType = toBarcodeType()
        val matrixEncoder = toMatrixEncoder()
        when {
            barcodeType != null -> barcodeType.encoder.encode(rawValue)
            matrixEncoder != null -> matrixEncoder.encode(rawValue)
            else -> QroseEncoders.QR(errorCorrection = errorCorrection).encode(rawValue)
        }
    }.isSuccess
}
package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import io.github.alexzhirkevich.qrose.oned.BarcodeType

/** Whether Scanny can draw a code of this format again from its text: a QR code, or a barcode. */
internal val BarcodeFormat.isDrawable: Boolean
    get() = this == BarcodeFormat.QR_CODE || toBarcodeType() != null

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
        // qrose draws no other code in two dimensions than the QR code
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF417,
        -> null
    }
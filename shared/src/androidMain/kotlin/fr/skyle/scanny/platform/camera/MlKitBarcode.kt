package fr.skyle.scanny.platform.camera

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.DetectedBarcode

internal val mlKitScannerOptions: BarcodeScannerOptions =
    BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

internal fun Barcode.toDetectedBarcode(): DetectedBarcode? {
    val text = rawValue ?: return null
    val barcodeFormat = when (format) {
        Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF417
        Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        Barcode.FORMAT_ITF -> BarcodeFormat.ITF
        Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        else -> return null
    }

    return DetectedBarcode(
        rawValue = text,
        format = barcodeFormat,
    )
}
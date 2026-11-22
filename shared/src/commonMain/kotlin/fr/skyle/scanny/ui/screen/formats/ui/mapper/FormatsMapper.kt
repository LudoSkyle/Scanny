package fr.skyle.scanny.ui.screen.formats.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat

/** Whether a code of this format is in two dimensions, rather than a barcode of bars in a row. */
internal val BarcodeFormat.isTwoDimensional: Boolean
    get() = when (this) {
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        // Rows of bars stacked: read as a code in two dimensions
        BarcodeFormat.PDF417,
        -> true

        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128,
        BarcodeFormat.ITF,
        BarcodeFormat.CODABAR,
        -> false
    }

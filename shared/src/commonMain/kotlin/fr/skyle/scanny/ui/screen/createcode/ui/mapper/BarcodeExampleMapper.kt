package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat

/**
 * A value a barcode of this format holds, shown under the field of the editor: the one its sheet in
 * the formats takes apart. Null for a code in two dimensions, which holds a content.
 */
internal val BarcodeFormat.exampleValue: String?
    get() = when (this) {
        BarcodeFormat.EAN_13 -> "4006381333931"
        BarcodeFormat.EAN_8 -> "96385074"
        BarcodeFormat.UPC_A -> "036000291452"
        BarcodeFormat.UPC_E -> "04252614"
        BarcodeFormat.CODE_39 -> "SCANNY"
        BarcodeFormat.CODE_93 -> "SCANNY"
        BarcodeFormat.CODE_128 -> "123456"
        BarcodeFormat.ITF -> "15400141288763"
        BarcodeFormat.CODABAR -> "A40156B"
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF417,
        -> null
    }

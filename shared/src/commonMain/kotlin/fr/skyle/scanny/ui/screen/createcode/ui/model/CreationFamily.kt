package fr.skyle.scanny.ui.screen.createcode.ui.model

/**
 * The family of the code the user creates: a QR code, another code in two dimensions holding the same
 * contents, or a barcode holding a value. Only a QR code is customized.
 */
enum class CreationFamily {
    QR_CODE,
    TWO_DIMENSIONAL,
    BARCODE,
}

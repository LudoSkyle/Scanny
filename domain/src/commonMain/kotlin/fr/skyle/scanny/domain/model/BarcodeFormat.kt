package fr.skyle.scanny.domain.model

/** The formats both detection engines read: ML Kit on Android, AVFoundation and Vision on iOS. */
enum class BarcodeFormat {
    QR_CODE,
    DATA_MATRIX,
    AZTEC,
    PDF417,
    EAN_13,
    EAN_8,
    UPC_A,
    UPC_E,
    CODE_39,
    CODE_93,
    CODE_128,
    ITF,
    CODABAR,
}
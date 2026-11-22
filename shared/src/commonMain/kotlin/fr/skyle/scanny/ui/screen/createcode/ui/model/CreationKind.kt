package fr.skyle.scanny.ui.screen.createcode.ui.model

/** What the user creates: a QR code holding one of these contents, or a barcode. */
enum class CreationKind {
    TEXT,
    LINK,
    EMAIL,
    SMS,
    MMS,
    PHONE,
    WIFI,
    CONTACT,
    LOCATION,
    BOOKMARK,
    OTP,
    BITCOIN,
    BARCODE,
}
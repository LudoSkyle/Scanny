package fr.skyle.scanny.ui.screen.createcode.ui.model

/** What a QR code or another code in two dimensions holds. */
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
    EVENT,
}
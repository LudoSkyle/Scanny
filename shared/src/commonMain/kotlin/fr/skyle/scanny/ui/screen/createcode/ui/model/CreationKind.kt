package fr.skyle.scanny.ui.screen.createcode.ui.model

/**
 * What a QR code or another code in two dimensions holds, from the most created to the least: the
 * order of the menu of the editor.
 */
enum class CreationKind {
    LINK,
    TEXT,
    WIFI,
    CONTACT,
    EMAIL,
    PHONE,
    SMS,
    LOCATION,
    EVENT,
    OTP,
    BOOKMARK,
    BITCOIN,
    MMS,
}

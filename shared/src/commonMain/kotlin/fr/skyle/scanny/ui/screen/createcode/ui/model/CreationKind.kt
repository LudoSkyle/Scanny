package fr.skyle.scanny.ui.screen.createcode.ui.model

/**
 * What a QR code or another code in two dimensions holds, from the most created to the least, the MMS
 * next to the SMS: the order of the menu of the editor.
 */
enum class CreationKind {
    TEXT,
    LINK,
    WIFI,
    CONTACT,
    EMAIL,
    PHONE,
    SMS,
    MMS,
    LOCATION,
    EVENT,
    OTP,
    BOOKMARK,
    BITCOIN,
}

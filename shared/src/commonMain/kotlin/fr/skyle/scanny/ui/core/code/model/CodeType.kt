package fr.skyle.scanny.ui.core.code.model

/** The kind of content of a code, as the screens name it and draw it. */
enum class CodeType {
    TEXT,
    LINK,
    EMAIL,
    SMS,
    MMS,
    WIFI,
    CONTACT,
    PHONE,
    LOCATION,
    BOOKMARK,
    OTP,
    BITCOIN,
    EVENT,
}
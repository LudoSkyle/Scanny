package fr.skyle.scanny.ui.screen.createcode.ui.model

/** What keeps a logo from going on a QR code: it needs the highest correction. */
enum class LogoBlocker {
    // A lower correction was chosen in the advanced options of the content
    CORRECTION_TOO_LOW,

    // The content is longer than the code holds at the highest correction
    CONTENT_TOO_LONG,
}

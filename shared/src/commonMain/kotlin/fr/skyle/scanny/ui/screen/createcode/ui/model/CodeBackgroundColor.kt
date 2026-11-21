package fr.skyle.scanny.ui.screen.createcode.ui.model

/** The colors the editor offers for the background of a QR code: light ones, under any [CodeDotColor]. */
enum class CodeBackgroundColor(val argb: Long) {
    WHITE(argb = 0xFFFFFFFF),
    CREAM(argb = 0xFFFFF8E1),
    LIGHT_BLUE(argb = 0xFFE3F2FD),
    LIGHT_GREEN(argb = 0xFFE8F5E9),
    PINK(argb = 0xFFFCE4EC),
    LAVENDER(argb = 0xFFEDE7F6),
}

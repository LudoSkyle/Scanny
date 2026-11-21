package fr.skyle.scanny.ui.screen.createcode.ui.model

/**
 * The colors the editor offers for the dots of a QR code: dark ones, for any scanner to read them on
 * any of the [CodeBackgroundColor]. A scanner reads poorly a light code on a dark background.
 */
enum class CodeDotColor(val argb: Long) {
    BLACK(argb = 0xFF000000),
    // The color of Scanny
    INDIGO(argb = 0xFF3B43BE),
    GREEN(argb = 0xFF1B5E20),
    RED(argb = 0xFFB71C1C),
    PURPLE(argb = 0xFF4A148C),
    BROWN(argb = 0xFF3E2723),
}

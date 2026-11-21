package fr.skyle.scanny.domain.model

/**
 * How a created QR code is drawn: the colors of its dots and of its background, the shapes of its dots
 * and of its three corners. A barcode keeps the default one, dark on white.
 */
data class CodeStyle(
    // ARGB
    val darkColor: Long = DEFAULT_DARK_COLOR,
    val lightColor: Long = DEFAULT_LIGHT_COLOR,
    val dotShape: CodeShape = CodeShape.SQUARE,
    val cornerShape: CodeShape = CodeShape.SQUARE,
) {

    companion object {
        const val DEFAULT_DARK_COLOR = 0xFF000000
        const val DEFAULT_LIGHT_COLOR = 0xFFFFFFFF
    }
}

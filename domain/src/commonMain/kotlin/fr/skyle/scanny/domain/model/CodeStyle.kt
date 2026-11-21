package fr.skyle.scanny.domain.model

/**
 * How a created QR code is drawn: the colors of its dots and of its background, the shapes of its dots
 * and of its three corners, and the logo at its center. A barcode keeps the default one, dark on white.
 */
data class CodeStyle(
    // ARGB
    val darkColor: Long = DEFAULT_DARK_COLOR,
    val lightColor: Long = DEFAULT_LIGHT_COLOR,
    val dotShape: CodeShape = CodeShape.SQUARE,
    val cornerShape: CodeShape = CodeShape.SQUARE,
    // A PNG, null for none
    val logo: ByteArray? = null,
) {

    // The logo compared by its bytes, not by its array
    override fun equals(other: Any?): Boolean =
        other is CodeStyle &&
            darkColor == other.darkColor &&
            lightColor == other.lightColor &&
            dotShape == other.dotShape &&
            cornerShape == other.cornerShape &&
            logo.contentEquals(other.logo)

    override fun hashCode(): Int {
        var result = darkColor.hashCode()
        result = 31 * result + lightColor.hashCode()
        result = 31 * result + dotShape.hashCode()
        result = 31 * result + cornerShape.hashCode()
        result = 31 * result + logo.contentHashCode()
        return result
    }

    companion object {
        const val DEFAULT_DARK_COLOR = 0xFF000000
        const val DEFAULT_LIGHT_COLOR = 0xFFFFFFFF
    }
}

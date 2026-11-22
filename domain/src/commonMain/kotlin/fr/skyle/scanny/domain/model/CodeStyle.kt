package fr.skyle.scanny.domain.model

/**
 * How a created QR code is drawn: the colors of its dots, with their gradient, of its three corners and
 * of its background, the shapes of its dots and of its corners, the logo at its center, and its error
 * correction. A barcode keeps the
 * default one, dark on white.
 */
data class CodeStyle(
    // ARGB
    val darkColor: Long = DEFAULT_DARK_COLOR,
    val lightColor: Long = DEFAULT_LIGHT_COLOR,
    // ARGB, null for the color of the dots
    val cornerColor: Long? = null,
    val gradient: CodeGradient = CodeGradient.NONE,
    // ARGB, the color the dots go to with a gradient
    val gradientColor: Long = DEFAULT_GRADIENT_COLOR,
    val dotShape: CodeShape = CodeShape.SQUARE,
    val cornerShape: CodeShape = CodeShape.SQUARE,
    // A PNG, null for none
    val logo: ByteArray? = null,
    val errorCorrection: CodeErrorCorrection = CodeErrorCorrection.AUTO,
) {

    // The logo compared by its bytes, not by its array
    override fun equals(other: Any?): Boolean =
        other is CodeStyle &&
            darkColor == other.darkColor &&
            lightColor == other.lightColor &&
            cornerColor == other.cornerColor &&
            gradient == other.gradient &&
            gradientColor == other.gradientColor &&
            dotShape == other.dotShape &&
            cornerShape == other.cornerShape &&
            logo.contentEquals(other.logo) &&
            errorCorrection == other.errorCorrection

    override fun hashCode(): Int {
        var result = darkColor.hashCode()
        result = 31 * result + lightColor.hashCode()
        result = 31 * result + cornerColor.hashCode()
        result = 31 * result + gradient.hashCode()
        result = 31 * result + gradientColor.hashCode()
        result = 31 * result + dotShape.hashCode()
        result = 31 * result + cornerShape.hashCode()
        result = 31 * result + logo.contentHashCode()
        result = 31 * result + errorCorrection.hashCode()
        return result
    }

    companion object {
        const val DEFAULT_DARK_COLOR = 0xFF000000
        const val DEFAULT_LIGHT_COLOR = 0xFFFFFFFF

        // The color of Scanny
        const val DEFAULT_GRADIENT_COLOR = 0xFF3B43BE
    }
}

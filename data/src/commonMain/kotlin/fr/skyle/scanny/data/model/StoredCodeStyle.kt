package fr.skyle.scanny.data.model

/** How a created code is drawn, as stored. */
data class StoredCodeStyle(
    // ARGB
    val darkColor: Long,
    val lightColor: Long,
    // ARGB, null for the color of the dots
    val cornerColor: Long?,
    // The name of a CodeGradient value of domain
    val gradient: String,
    // ARGB
    val gradientColor: Long,
    // The names of CodeShape values of domain
    val dotShape: String,
    val cornerShape: String,
    // A PNG, null for none
    val logo: ByteArray?,
    // The name of a CodeErrorCorrection value of domain
    val errorCorrection: String,
) {

    // The logo compared by its bytes, not by its array
    override fun equals(other: Any?): Boolean =
        other is StoredCodeStyle &&
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
}

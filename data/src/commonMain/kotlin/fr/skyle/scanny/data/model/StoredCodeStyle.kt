package fr.skyle.scanny.data.model

/** How a created code is drawn, as stored. */
data class StoredCodeStyle(
    // ARGB
    val darkColor: Long,
    val lightColor: Long,
    // The names of CodeShape values of domain
    val dotShape: String,
    val cornerShape: String,
    // A PNG, null for none
    val logo: ByteArray?,
) {

    // The logo compared by its bytes, not by its array
    override fun equals(other: Any?): Boolean =
        other is StoredCodeStyle &&
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
}

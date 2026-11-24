package fr.skyle.scanny.platform.image

/**
 * An image kept on the device, as the device finds it again: its content URI on Android, the local
 * identifier of its asset on iOS, or the URL of its file there for an SVG image, which the photos keep
 * none of.
 */
data class SavedImage(
    val id: String,
    val mimeType: String = PNG_MIME_TYPE,
) {

    val isSvg: Boolean
        get() = mimeType == SVG_MIME_TYPE

    companion object {
        const val PNG_MIME_TYPE = "image/png"
        const val SVG_MIME_TYPE = "image/svg+xml"
    }
}

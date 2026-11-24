package fr.skyle.scanny.platform.image

/** Keeps an image on the device: a PNG in the photos, an SVG image in the files. */
interface ImageSaver {

    /** Whether the device lets the app keep an image there, as it asks no permission for it. */
    val canSave: Boolean

    /** Keeps [png] in the photos, named [fileName], and returns where; throws when the device refuses it. */
    suspend fun saveImage(
        png: ByteArray,
        fileName: String,
    ): SavedImage

    /**
     * Keeps [svg] in the files, named [fileName], and returns where, or null when the user chose not to:
     * iOS asks where. Throws when the device refuses it.
     */
    suspend fun saveSvg(
        svg: String,
        fileName: String,
    ): SavedImage?
}

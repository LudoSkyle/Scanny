package fr.skyle.scanny.platform.image

/** Keeps an image in the photos of the device. */
interface ImageSaver {

    /** Whether the device lets the app keep an image there, as it asks no permission for it. */
    val canSave: Boolean

    /** Keeps [png] in the photos, named [fileName], and returns where; throws when the device refuses it. */
    suspend fun saveImage(
        png: ByteArray,
        fileName: String,
    ): SavedImage
}
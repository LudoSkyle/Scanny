package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.image.ImageSaver
import fr.skyle.scanny.platform.image.SavedImage

/** Keeps the images it is given, PNG and SVG, or fails if [error] is set; [isSvgCancelled] as a user who keeps none. */
internal class FakeImageSaver(
    override val canSave: Boolean = true,
    private val error: Exception? = null,
    private val isSvgCancelled: Boolean = false,
) : ImageSaver {

    val savedImages = mutableListOf<Pair<List<Byte>, String>>()
    val savedSvgs = mutableListOf<Pair<String, String>>()

    override suspend fun saveImage(
        png: ByteArray,
        fileName: String,
    ): SavedImage {
        error?.let { throw it }

        savedImages += png.toList() to fileName
        return SavedImage(id = "saved/$fileName")
    }

    override suspend fun saveSvg(
        svg: String,
        fileName: String,
    ): SavedImage? {
        error?.let { throw it }
        if (isSvgCancelled) {
            return null
        }

        savedSvgs += svg to fileName
        return SavedImage(
            id = "saved/$fileName",
            mimeType = SavedImage.SVG_MIME_TYPE,
        )
    }
}

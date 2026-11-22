package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.image.ImageSaver
import fr.skyle.scanny.platform.image.SavedImage

/** Keeps the images it is given, or fails if [error] is set. */
internal class FakeImageSaver(
    override val canSave: Boolean = true,
    private val error: Exception? = null,
) : ImageSaver {

    val savedImages = mutableListOf<Pair<List<Byte>, String>>()

    override suspend fun saveImage(
        png: ByteArray,
        fileName: String,
    ): SavedImage {
        error?.let { throw it }

        savedImages += png.toList() to fileName
        return SavedImage(id = "saved/$fileName")
    }
}
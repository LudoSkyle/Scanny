package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.share.ImageSharer

/** Keeps the images it is given to share, or fails if [error] is set. */
internal class FakeImageSharer(
    private val error: Exception? = null,
) : ImageSharer {

    val sharedImages = mutableListOf<Pair<List<Byte>, String>>()

    override fun shareImage(
        png: ByteArray,
        fileName: String,
    ) {
        error?.let { throw it }

        sharedImages += png.toList() to fileName
    }
}

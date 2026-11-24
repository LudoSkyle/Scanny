package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.share.ImageSharer

/** Keeps the images it is given to share, PNG and SVG, or fails if [error] is set. */
internal class FakeImageSharer(
    private val error: Exception? = null,
) : ImageSharer {

    val sharedImages = mutableListOf<Pair<List<Byte>, String>>()
    val sharedSvgs = mutableListOf<Pair<String, String>>()

    override fun shareImage(
        png: ByteArray,
        fileName: String,
    ) {
        error?.let { throw it }

        sharedImages += png.toList() to fileName
    }

    override fun shareSvg(
        svg: String,
        fileName: String,
    ) {
        error?.let { throw it }

        sharedSvgs += svg to fileName
    }
}

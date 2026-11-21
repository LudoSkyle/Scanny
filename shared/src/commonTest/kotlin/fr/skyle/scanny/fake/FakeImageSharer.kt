package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.share.ImageSharer

internal class FakeImageSharer : ImageSharer {

    val sharedImages = mutableListOf<Pair<List<Byte>, String>>()

    override fun shareImage(
        png: ByteArray,
        fileName: String,
    ) {
        sharedImages += png.toList() to fileName
    }
}
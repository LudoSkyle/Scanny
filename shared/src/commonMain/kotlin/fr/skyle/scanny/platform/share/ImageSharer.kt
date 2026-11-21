package fr.skyle.scanny.platform.share

/** Opens the share sheet of the system on an image, a PNG, named [fileName] for the apps that keep it. */
interface ImageSharer {

    fun shareImage(
        png: ByteArray,
        fileName: String,
    )
}
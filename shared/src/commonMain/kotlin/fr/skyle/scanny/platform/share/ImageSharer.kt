package fr.skyle.scanny.platform.share

/** Opens the share sheet of the system on an image, named [fileName] for the apps that keep it. */
interface ImageSharer {

    /** A PNG, which the share sheet may keep in the photos. */
    fun shareImage(
        png: ByteArray,
        fileName: String,
    )

    /** An SVG image, a file for the apps that open or keep one: the photos keep none. */
    fun shareSvg(
        svg: String,
        fileName: String,
    )
}

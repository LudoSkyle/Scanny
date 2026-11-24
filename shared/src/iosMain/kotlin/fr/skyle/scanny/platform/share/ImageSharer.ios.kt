package fr.skyle.scanny.platform.share

import fr.skyle.scanny.platform.image.toNSData
import fr.skyle.scanny.platform.ui.topViewController
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImage

internal class IosImageSharer : ImageSharer {

    // An image rather than a file: the share sheet then offers to keep it in the photos
    override fun shareImage(
        png: ByteArray,
        fileName: String,
    ) {
        share(item = UIImage(data = png.toNSData()))
    }

    // A file, named, for Files and the apps that open an SVG image
    override fun shareSvg(
        svg: String,
        fileName: String,
    ) {
        val url = checkNotNull(NSURL.fileURLWithPath(path = NSTemporaryDirectory()).URLByAppendingPathComponent(fileName)) {
            "No temporary file for the SVG image $fileName"
        }
        check(svg.encodeToByteArray().toNSData().writeToURL(url = url, atomically = true)) {
            "Failed to write the SVG image $fileName"
        }
        share(item = url)
    }

    private fun share(item: Any) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(item),
            applicationActivities = null,
        )

        topViewController()?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }
}
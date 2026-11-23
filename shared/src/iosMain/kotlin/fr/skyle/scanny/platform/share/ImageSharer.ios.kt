package fr.skyle.scanny.platform.share

import fr.skyle.scanny.platform.image.toNSData
import fr.skyle.scanny.platform.ui.topViewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImage

internal class IosImageSharer : ImageSharer {

    // An image rather than a file: the share sheet then offers to keep it in the photos
    override fun shareImage(
        png: ByteArray,
        fileName: String,
    ) {
        val image = UIImage(data = png.toNSData())
        val activityViewController = UIActivityViewController(
            activityItems = listOf(image),
            applicationActivities = null,
        )

        topViewController()?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }
}
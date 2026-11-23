package fr.skyle.scanny.platform.share

import fr.skyle.scanny.platform.ui.topViewController
import platform.UIKit.UIActivityViewController

internal class IosTextSharer : TextSharer {

    override fun shareText(text: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )

        topViewController()?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }
}
package fr.skyle.scanny.platform.ui

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

// Looked up when an action needs it, and never during the composition
internal fun topViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { window -> window.isKeyWindow() }

    var viewController = keyWindow?.rootViewController
    while (viewController?.presentedViewController != null) {
        viewController = viewController.presentedViewController
    }
    return viewController
}
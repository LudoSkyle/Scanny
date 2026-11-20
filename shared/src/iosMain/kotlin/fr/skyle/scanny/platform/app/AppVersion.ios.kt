package fr.skyle.scanny.platform.app

import platform.Foundation.NSBundle

internal class IosAppVersion : AppVersion {

    override val name: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""

    override val build: String =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: ""
}
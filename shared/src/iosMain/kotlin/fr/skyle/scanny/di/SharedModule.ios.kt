package fr.skyle.scanny.di

import fr.skyle.scanny.platform.app.AppVersion
import fr.skyle.scanny.platform.app.IosAppVersion
import fr.skyle.scanny.platform.contact.ContactAdder
import fr.skyle.scanny.platform.contact.IosContactAdder
import fr.skyle.scanny.platform.share.IosTextSharer
import fr.skyle.scanny.platform.share.TextSharer
import fr.skyle.scanny.platform.wifi.IosWifiNetworkSupport
import fr.skyle.scanny.platform.wifi.WifiNetworkSupport
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformSharedModule: Module = module {
    singleOf(::IosTextSharer) bind TextSharer::class
    singleOf(::IosAppVersion) bind AppVersion::class
    singleOf(::IosContactAdder) bind ContactAdder::class
    singleOf(::IosWifiNetworkSupport) bind WifiNetworkSupport::class
}
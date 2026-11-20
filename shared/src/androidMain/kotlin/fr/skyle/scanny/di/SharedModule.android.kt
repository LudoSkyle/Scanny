package fr.skyle.scanny.di

import android.os.Build
import fr.skyle.scanny.platform.app.AndroidAppVersion
import fr.skyle.scanny.platform.app.AppVersion
import fr.skyle.scanny.platform.contact.AndroidContactAdder
import fr.skyle.scanny.platform.contact.ContactAdder
import fr.skyle.scanny.platform.share.AndroidTextSharer
import fr.skyle.scanny.platform.share.TextSharer
import fr.skyle.scanny.platform.sound.AndroidScanBeeper
import fr.skyle.scanny.platform.sound.ScanBeeper
import fr.skyle.scanny.platform.wifi.AndroidWifiNetworkSupport
import fr.skyle.scanny.platform.wifi.WifiNetworkSupport
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformSharedModule: Module = module {
    singleOf(::AndroidTextSharer) bind TextSharer::class
    singleOf(::AndroidAppVersion) bind AppVersion::class
    singleOf(::AndroidScanBeeper) bind ScanBeeper::class
    singleOf(::AndroidContactAdder) bind ContactAdder::class
    single<WifiNetworkSupport> { AndroidWifiNetworkSupport(sdkInt = Build.VERSION.SDK_INT) }
}
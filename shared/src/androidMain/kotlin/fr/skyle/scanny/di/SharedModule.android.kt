package fr.skyle.scanny.di

import android.os.Build
import fr.skyle.scanny.platform.app.AndroidAppVersion
import fr.skyle.scanny.platform.app.AppVersion
import fr.skyle.scanny.platform.calendar.AndroidEventAdder
import fr.skyle.scanny.platform.calendar.EventAdder
import fr.skyle.scanny.platform.contact.AndroidContactAdder
import fr.skyle.scanny.platform.contact.ContactAdder
import fr.skyle.scanny.platform.image.AndroidImageSaver
import fr.skyle.scanny.platform.image.ImageSaver
import fr.skyle.scanny.platform.share.AndroidImageSharer
import fr.skyle.scanny.platform.share.AndroidTextSharer
import fr.skyle.scanny.platform.share.ImageSharer
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
    singleOf(::AndroidImageSharer) bind ImageSharer::class
    single<ImageSaver> {
        AndroidImageSaver(
            context = get(),
            sdkInt = Build.VERSION.SDK_INT,
        )
    }
    singleOf(::AndroidAppVersion) bind AppVersion::class
    singleOf(::AndroidScanBeeper) bind ScanBeeper::class
    singleOf(::AndroidContactAdder) bind ContactAdder::class
    singleOf(::AndroidEventAdder) bind EventAdder::class
    single<WifiNetworkSupport> { AndroidWifiNetworkSupport() }
}
package fr.skyle.scanny.di

import fr.skyle.scanny.domain.di.domainModule
import fr.skyle.scanny.ui.screen.about.ui.AboutViewModel
import fr.skyle.scanny.ui.screen.scan.ui.ScanViewModel
import fr.skyle.scanny.ui.screen.scandetail.ui.ScanDetailViewModel
import fr.skyle.scanny.ui.screen.scangroup.ui.ScanGroupViewModel
import fr.skyle.scanny.ui.screen.scanhistory.ui.ScanHistoryViewModel
import fr.skyle.scanny.ui.screen.settings.ui.SettingsViewModel
import kotlinx.datetime.TimeZone
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    includes(
        domainModule,
        platformSharedModule,
    )

    // Read at each screen: the user may change it while the app runs
    factory { TimeZone.currentSystemDefault() }

    viewModelOf(::ScanViewModel)
    viewModelOf(::ScanDetailViewModel)
    viewModelOf(::ScanGroupViewModel)
    viewModelOf(::ScanHistoryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AboutViewModel)
}

// The native actions, implemented by each platform
internal expect val platformSharedModule: Module
package fr.skyle.scanny.di

import fr.skyle.scanny.domain.di.domainModule
import fr.skyle.scanny.ui.core.code.image.CodeImageRenderer
import fr.skyle.scanny.ui.core.code.image.QroseCodeImageRenderer
import fr.skyle.scanny.ui.main.MainViewModel
import fr.skyle.scanny.ui.screen.createcode.ui.CreateCodeViewModel
import fr.skyle.scanny.ui.screen.creationdetail.ui.CreationDetailViewModel
import fr.skyle.scanny.ui.screen.scan.ui.ScanViewModel
import fr.skyle.scanny.ui.screen.scandetail.ui.ScanDetailViewModel
import fr.skyle.scanny.ui.screen.scangroup.ui.ScanGroupViewModel
import fr.skyle.scanny.ui.screen.history.ui.HistoryViewModel
import fr.skyle.scanny.ui.screen.settings.ui.SettingsViewModel
import kotlinx.datetime.TimeZone
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    includes(
        domainModule,
        platformSharedModule,
    )

    // Read at each screen: the user may change it while the app runs
    factory { TimeZone.currentSystemDefault() }

    single<CodeImageRenderer> { QroseCodeImageRenderer() }

    viewModelOf(::MainViewModel)
    viewModelOf(::ScanViewModel)
    viewModelOf(::ScanDetailViewModel)
    viewModelOf(::ScanGroupViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
    // Opened empty from the scan, with no scanned code to fill it
    viewModel { parameters ->
        CreateCodeViewModel(
            scannedCodeId = parameters.getOrNull(),
            watchScannedCodeUseCase = get(),
            recordCreationUseCase = get(),
        )
    }
    viewModelOf(::CreationDetailViewModel)
}

// The native actions, implemented by each platform
internal expect val platformSharedModule: Module
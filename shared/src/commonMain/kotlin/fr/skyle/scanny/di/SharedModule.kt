package fr.skyle.scanny.di

import fr.skyle.scanny.domain.di.domainModule
import fr.skyle.scanny.ui.screen.scan.ui.ScanViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    includes(
        domainModule,
        platformSharedModule,
    )

    viewModelOf(::ScanViewModel)
}

// The native actions, implemented by each platform
internal expect val platformSharedModule: Module
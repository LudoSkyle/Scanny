package fr.skyle.scanny.di

import fr.skyle.scanny.platform.share.IosTextSharer
import fr.skyle.scanny.platform.share.TextSharer
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformSharedModule: Module = module {
    singleOf(::IosTextSharer) bind TextSharer::class
}
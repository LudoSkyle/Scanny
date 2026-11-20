package fr.skyle.scanny.di

import fr.skyle.scanny.domain.di.domainModule
import org.koin.dsl.module

val sharedModule = module {
    includes(domainModule)
}
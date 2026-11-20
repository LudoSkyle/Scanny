package fr.skyle.scanny.domain.di

import fr.skyle.scanny.data.di.dataModule
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)
}
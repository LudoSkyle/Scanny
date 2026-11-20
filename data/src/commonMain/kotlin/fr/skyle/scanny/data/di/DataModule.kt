package fr.skyle.scanny.data.di

import fr.skyle.scanny.data.db.ScannyDatabase
import fr.skyle.scanny.data.repository.scan.ScanRepositoryImpl
import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.data.repository.settings.SettingsRepositoryImpl
import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    includes(platformDataModule)

    single { get<ScannyDatabase>().scanDao() }

    singleOf(::ScanRepositoryImpl) bind ScanRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
}

// The database and the settings file, whose location depends on the platform
internal expect val platformDataModule: Module
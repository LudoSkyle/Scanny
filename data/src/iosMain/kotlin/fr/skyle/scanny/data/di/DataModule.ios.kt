package fr.skyle.scanny.data.di

import fr.skyle.scanny.data.db.ScannyDatabase
import fr.skyle.scanny.data.db.build
import fr.skyle.scanny.data.file.applicationSupportPath
import fr.skyle.scanny.data.preferences.PREFERENCES_FILE_NAME
import fr.skyle.scanny.data.preferences.createPreferencesDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    single { ScannyDatabase.build() }

    single { createPreferencesDataStore(path = applicationSupportPath(fileName = PREFERENCES_FILE_NAME)) }
}
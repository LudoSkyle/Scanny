package fr.skyle.scanny.data.di

import fr.skyle.scanny.data.db.ScannyDatabase
import fr.skyle.scanny.data.db.build
import fr.skyle.scanny.data.preferences.PREFERENCES_FILE_NAME
import fr.skyle.scanny.data.preferences.createPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    single { ScannyDatabase.build(context = androidContext()) }

    single {
        // Where preferencesDataStore() of the v1 kept it
        val file = androidContext().filesDir.resolve("datastore/$PREFERENCES_FILE_NAME")
        createPreferencesDataStore(path = file.absolutePath)
    }
}
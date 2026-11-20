package fr.skyle.scanny.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import okio.Path.Companion.toPath

// The name of the file of the v1, so that the settings of the Android users survive the update
internal const val PREFERENCES_FILE_NAME = "sc_datastore.preferences_pb"

// A corrupted settings file is a fact of the device and not a bug: the settings start again from
// their default values instead of failing every read
internal fun createPreferencesDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler {
            emptyPreferences()
        },
        produceFile = {
            path.toPath()
        },
    )
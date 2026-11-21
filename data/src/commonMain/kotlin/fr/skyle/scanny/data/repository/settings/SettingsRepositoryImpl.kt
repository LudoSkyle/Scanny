package fr.skyle.scanny.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun watchIsVibrationAfterScanEnabled(): Flow<Boolean> =
        watch(
            key = KEY_VIBRATION_AFTER_SCAN,
            defaultValue = true,
        )

    override suspend fun setVibrationAfterScanEnabled(isEnabled: Boolean) {
        set(
            key = KEY_VIBRATION_AFTER_SCAN,
            value = isEnabled,
        )
    }

    override fun watchIsBeepAfterScanEnabled(): Flow<Boolean> =
        watch(
            key = KEY_BEEP_AFTER_SCAN,
            defaultValue = false,
        )

    override suspend fun setBeepAfterScanEnabled(isEnabled: Boolean) {
        set(
            key = KEY_BEEP_AFTER_SCAN,
            value = isEnabled,
        )
    }

    override fun watchIsCopyAfterScanEnabled(): Flow<Boolean> =
        watch(
            key = KEY_COPY_AFTER_SCAN,
            defaultValue = false,
        )

    override suspend fun setCopyAfterScanEnabled(isEnabled: Boolean) {
        set(
            key = KEY_COPY_AFTER_SCAN,
            value = isEnabled,
        )
    }

    override fun watchIsOpenContentAfterScanEnabled(): Flow<Boolean> =
        watch(
            key = KEY_OPEN_LINK_AFTER_SCAN,
            defaultValue = false,
        )

    override suspend fun setOpenContentAfterScanEnabled(isEnabled: Boolean) {
        set(
            key = KEY_OPEN_LINK_AFTER_SCAN,
            value = isEnabled,
        )
    }

    override fun watchIsRawContentShown(): Flow<Boolean> =
        watch(
            key = KEY_RAW_CONTENT_SHOWN,
            defaultValue = false,
        )

    override suspend fun setRawContentShown(isShown: Boolean) {
        set(
            key = KEY_RAW_CONTENT_SHOWN,
            value = isShown,
        )
    }

    private fun watch(
        key: Preferences.Key<Boolean>,
        defaultValue: Boolean,
    ): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
            .distinctUntilChanged()

    private suspend fun set(
        key: Preferences.Key<Boolean>,
        value: Boolean,
    ) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    internal companion object {
        // The keys of the v1, so that the settings of the Android users survive the update
        val KEY_VIBRATION_AFTER_SCAN = booleanPreferencesKey(name = "KEY_IS_VIBRATION_AFTER_SCAN_ENABLED")
        // The name of v1, where the setting opened the links only
        val KEY_OPEN_LINK_AFTER_SCAN = booleanPreferencesKey(name = "KEY_OPEN_LINK_AFTER_SCAN")
        val KEY_RAW_CONTENT_SHOWN = booleanPreferencesKey(name = "KEY_IS_RAW_CONTENT_SHOWN")

        // New with the v2
        val KEY_BEEP_AFTER_SCAN = booleanPreferencesKey(name = "KEY_IS_BEEP_AFTER_SCAN_ENABLED")
        val KEY_COPY_AFTER_SCAN = booleanPreferencesKey(name = "KEY_IS_COPY_AFTER_SCAN_ENABLED")
    }
}
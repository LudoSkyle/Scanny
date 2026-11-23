package fr.skyle.scanny.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    override fun watchIsFrontCameraUsed(): Flow<Boolean> =
        watch(
            key = KEY_FRONT_CAMERA,
            defaultValue = false,
        )

    override suspend fun setFrontCameraUsed(isUsed: Boolean) {
        set(
            key = KEY_FRONT_CAMERA,
            value = isUsed,
        )
    }

    override fun watchIsScanHistoryEnabled(): Flow<Boolean> =
        watch(
            key = KEY_SCAN_HISTORY,
            defaultValue = true,
        )

    override suspend fun setScanHistoryEnabled(isEnabled: Boolean) {
        set(
            key = KEY_SCAN_HISTORY,
            value = isEnabled,
        )
    }

    override fun watchIsCreationHistoryEnabled(): Flow<Boolean> =
        watch(
            key = KEY_CREATION_HISTORY,
            defaultValue = true,
        )

    override suspend fun setCreationHistoryEnabled(isEnabled: Boolean) {
        set(
            key = KEY_CREATION_HISTORY,
            value = isEnabled,
        )
    }

    override fun watchIsConsecutiveDuplicateSkipped(): Flow<Boolean> =
        watch(
            key = KEY_CONSECUTIVE_DUPLICATE_SKIPPED,
            defaultValue = true,
        )

    override suspend fun setConsecutiveDuplicateSkipped(isSkipped: Boolean) {
        set(
            key = KEY_CONSECUTIVE_DUPLICATE_SKIPPED,
            value = isSkipped,
        )
    }

    override fun watchHistoryRetentionDays(): Flow<Int> =
        watch(
            key = KEY_HISTORY_RETENTION_DAYS,
            defaultValue = DEFAULT_HISTORY_RETENTION_DAYS,
        )

    override suspend fun setHistoryRetentionDays(days: Int) {
        set(
            key = KEY_HISTORY_RETENTION_DAYS,
            value = days,
        )
    }

    private fun <T> watch(
        key: Preferences.Key<T>,
        defaultValue: T,
    ): Flow<T> =
        dataStore.data
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
            .distinctUntilChanged()

    private suspend fun <T> set(
        key: Preferences.Key<T>,
        value: T,
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
        // Where the v1 showed the raw content, the v2 shows it in the sheet of a scan
        val KEY_RAW_CONTENT_SHOWN = booleanPreferencesKey(name = "KEY_IS_RAW_CONTENT_SHOWN")

        // New with the v2
        val KEY_BEEP_AFTER_SCAN = booleanPreferencesKey(name = "KEY_IS_BEEP_AFTER_SCAN_ENABLED")
        val KEY_COPY_AFTER_SCAN = booleanPreferencesKey(name = "KEY_IS_COPY_AFTER_SCAN_ENABLED")
        val KEY_FRONT_CAMERA = booleanPreferencesKey(name = "KEY_IS_FRONT_CAMERA_USED")
        val KEY_SCAN_HISTORY = booleanPreferencesKey(name = "KEY_IS_SCAN_HISTORY_ENABLED")
        val KEY_CREATION_HISTORY = booleanPreferencesKey(name = "KEY_IS_CREATION_HISTORY_ENABLED")
        val KEY_CONSECUTIVE_DUPLICATE_SKIPPED = booleanPreferencesKey(name = "KEY_IS_CONSECUTIVE_DUPLICATE_SKIPPED")
        val KEY_HISTORY_RETENTION_DAYS = intPreferencesKey(name = "KEY_HISTORY_RETENTION_DAYS")

        const val DEFAULT_HISTORY_RETENTION_DAYS = 30
    }
}
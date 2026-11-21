package fr.skyle.scanny.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryImplTest {

    @Test
    fun `the settings have the default values of the v1 and the new ones are off`() = runTest {
        // Given
        val settingsRepository = SettingsRepositoryImpl(dataStore = inMemoryDataStore())

        // When
        val settings = settingsRepository.currentSettings()

        // Then
        assertEquals(
            expected = listOf(
                true,
                false,
                false,
                false,
                false,
            ),
            actual = settings,
        )
    }

    @Test
    fun `a changed setting is watched with its new value`() = runTest {
        // Given
        val settingsRepository = SettingsRepositoryImpl(dataStore = inMemoryDataStore())

        // When
        settingsRepository.setVibrationAfterScanEnabled(isEnabled = false)
        settingsRepository.setOpenContentAfterScanEnabled(isEnabled = true)
        settingsRepository.setRawContentShown(isShown = true)
        settingsRepository.setBeepAfterScanEnabled(isEnabled = true)
        settingsRepository.setCopyAfterScanEnabled(isEnabled = true)

        // Then
        assertEquals(
            expected = listOf(
                false,
                true,
                true,
                true,
                true,
            ),
            actual = settingsRepository.currentSettings(),
        )
    }

    @Test
    fun `the settings are stored under the keys of the v1`() = runTest {
        // Given
        val dataStore = inMemoryDataStore()
        val settingsRepository = SettingsRepositoryImpl(dataStore = dataStore)

        // When
        settingsRepository.setVibrationAfterScanEnabled(isEnabled = false)
        settingsRepository.setOpenContentAfterScanEnabled(isEnabled = true)
        settingsRepository.setRawContentShown(isShown = true)
        settingsRepository.setBeepAfterScanEnabled(isEnabled = true)
        settingsRepository.setCopyAfterScanEnabled(isEnabled = false)

        // Then
        assertEquals(
            expected = mapOf(
                "KEY_IS_VIBRATION_AFTER_SCAN_ENABLED" to false,
                "KEY_OPEN_LINK_AFTER_SCAN" to true,
                "KEY_IS_RAW_CONTENT_SHOWN" to true,
                "KEY_IS_BEEP_AFTER_SCAN_ENABLED" to true,
                "KEY_IS_COPY_AFTER_SCAN_ENABLED" to false,
            ),
            actual = dataStore.data.first().asMap().mapKeys { (key, _) -> key.name },
        )
    }

    @Test
    fun `the history keeps the scans for 30 days and skips the consecutive duplicates by default`() = runTest {
        // Given
        val settingsRepository = SettingsRepositoryImpl(dataStore = inMemoryDataStore())

        // Then
        assertEquals(
            expected = Triple(true, true, 30),
            actual = Triple(
                settingsRepository.watchIsScanHistoryEnabled().first(),
                settingsRepository.watchIsConsecutiveDuplicateSkipped().first(),
                settingsRepository.watchHistoryRetentionDays().first(),
            ),
        )
    }

    @Test
    fun `the history settings are watched with their new values and stored under their keys`() = runTest {
        // Given
        val dataStore = inMemoryDataStore()
        val settingsRepository = SettingsRepositoryImpl(dataStore = dataStore)

        // When
        settingsRepository.setScanHistoryEnabled(isEnabled = false)
        settingsRepository.setConsecutiveDuplicateSkipped(isSkipped = false)
        settingsRepository.setHistoryRetentionDays(days = 0)

        // Then
        assertEquals(
            expected = Triple(false, false, 0),
            actual = Triple(
                settingsRepository.watchIsScanHistoryEnabled().first(),
                settingsRepository.watchIsConsecutiveDuplicateSkipped().first(),
                settingsRepository.watchHistoryRetentionDays().first(),
            ),
        )
        assertEquals(
            expected = mapOf(
                "KEY_IS_SCAN_HISTORY_ENABLED" to false,
                "KEY_IS_CONSECUTIVE_DUPLICATE_SKIPPED" to false,
                "KEY_HISTORY_RETENTION_DAYS" to 0,
            ),
            actual = dataStore.data.first().asMap().mapKeys { (key, _) -> key.name },
        )
    }

    // In the order of the v1 settings, then the new ones
    private suspend fun SettingsRepositoryImpl.currentSettings(): List<Boolean> =
        listOf(
            watchIsVibrationAfterScanEnabled().first(),
            watchIsOpenContentAfterScanEnabled().first(),
            watchIsRawContentShown().first(),
            watchIsBeepAfterScanEnabled().first(),
            watchIsCopyAfterScanEnabled().first(),
        )

    // A file system in memory, new for each test: a DataStore refuses two instances on the same file
    private fun TestScope.inMemoryDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FakeFileSystem(),
                serializer = PreferencesSerializer,
                producePath = {
                    "/settings.preferences_pb".toPath()
                },
            ),
            scope = backgroundScope,
        )
}
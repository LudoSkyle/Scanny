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
    fun `the settings have the default values of the v1`() = runTest {
        // Given
        val settingsRepository = SettingsRepositoryImpl(dataStore = inMemoryDataStore())

        // When
        val settings = listOf(
            settingsRepository.watchIsVibrationAfterScanEnabled().first(),
            settingsRepository.watchIsOpenLinkAfterScanEnabled().first(),
            settingsRepository.watchIsRawContentShown().first(),
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
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
        settingsRepository.setOpenLinkAfterScanEnabled(isEnabled = true)
        settingsRepository.setRawContentShown(isShown = true)

        // Then
        assertEquals(
            expected = listOf(
                false,
                true,
                true,
            ),
            actual = listOf(
                settingsRepository.watchIsVibrationAfterScanEnabled().first(),
                settingsRepository.watchIsOpenLinkAfterScanEnabled().first(),
                settingsRepository.watchIsRawContentShown().first(),
            ),
        )
    }

    @Test
    fun `the settings are stored under the keys of the v1`() = runTest {
        // Given
        val dataStore = inMemoryDataStore()
        val settingsRepository = SettingsRepositoryImpl(dataStore = dataStore)

        // When
        settingsRepository.setVibrationAfterScanEnabled(isEnabled = false)
        settingsRepository.setOpenLinkAfterScanEnabled(isEnabled = true)
        settingsRepository.setRawContentShown(isShown = true)

        // Then
        assertEquals(
            expected = mapOf(
                "KEY_IS_VIBRATION_AFTER_SCAN_ENABLED" to false,
                "KEY_OPEN_LINK_AFTER_SCAN" to true,
                "KEY_IS_RAW_CONTENT_SHOWN" to true,
            ),
            actual = dataStore.data.first().asMap().mapKeys { (key, _) -> key.name },
        )
    }

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
package fr.skyle.scanny.domain.usecase.settings

import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.model.ScanSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class WatchScanSettingsUseCaseTest {

    private val settingsRepository = FakeSettingsRepository(
        isVibrationAfterScanEnabled = true,
        isBeepAfterScanEnabled = true,
        isCopyAfterScanEnabled = false,
        isOpenContentAfterScanEnabled = false,
        isRawContentShown = true,
        isFrontCameraUsed = false,
    )

    private val watchScanSettingsUseCase = WatchScanSettingsUseCaseImpl(settingsRepository = settingsRepository)

    @Test
    fun `the settings are watched together`() = runTest {
        // When
        val settings = watchScanSettingsUseCase().first()

        // Then
        assertEquals(
            expected = ScanSettings(
                isVibrationAfterScanEnabled = true,
                isBeepAfterScanEnabled = true,
                isCopyAfterScanEnabled = false,
                isOpenContentAfterScanEnabled = false,
                isRawContentShown = true,
                isFrontCameraUsed = false,
            ),
            actual = settings,
        )
    }

    @Test
    fun `changing one setting emits the settings again`() = runTest {
        // Given
        val emittedSettings = mutableListOf<ScanSettings>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            watchScanSettingsUseCase().toList(emittedSettings)
        }

        // When
        settingsRepository.setOpenContentAfterScanEnabled(isEnabled = true)

        // Then
        assertEquals(
            expected = listOf(
                ScanSettings(
                    isVibrationAfterScanEnabled = true,
                    isBeepAfterScanEnabled = true,
                    isCopyAfterScanEnabled = false,
                    isOpenContentAfterScanEnabled = false,
                    isRawContentShown = true,
                    isFrontCameraUsed = false,
                ),
                ScanSettings(
                    isVibrationAfterScanEnabled = true,
                    isBeepAfterScanEnabled = true,
                    isCopyAfterScanEnabled = false,
                    isOpenContentAfterScanEnabled = true,
                    isRawContentShown = true,
                    isFrontCameraUsed = false,
                ),
            ),
            actual = emittedSettings,
        )
    }
}
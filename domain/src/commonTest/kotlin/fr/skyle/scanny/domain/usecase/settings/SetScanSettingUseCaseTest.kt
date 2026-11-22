package fr.skyle.scanny.domain.usecase.settings

import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.model.ScanSetting
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetScanSettingUseCaseTest {

    private val settingsRepository = FakeSettingsRepository(
        isVibrationAfterScanEnabled = true,
        isOpenContentAfterScanEnabled = false,
        isFrontCameraUsed = false,
    )

    private val setScanSettingUseCase = SetScanSettingUseCaseImpl(settingsRepository = settingsRepository)

    @Test
    fun `the vibration after a scan changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.VIBRATION_AFTER_SCAN,
            isEnabled = false,
        )

        // Then
        assertEquals(
            expected = listOf(
                false,
                false,
                false,
                false,
                false,
                false,
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the opening of the content after a scan changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.OPEN_CONTENT_AFTER_SCAN,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
                true,
                false,
                false,
                false,
                false,
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the beep after a scan changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.BEEP_AFTER_SCAN,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
                false,
                true,
                false,
                false,
                false,
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the copy after a scan changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.COPY_AFTER_SCAN,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
                false,
                false,
                true,
                false,
                false,
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the use of the front camera changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.FRONT_CAMERA,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
                false,
                false,
                false,
                true,
                false,
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the raw content in the sheet of a scan changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.RAW_CONTENT_SHOWN,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
                false,
                false,
                false,
                false,
                true,
            ),
            actual = currentSettings(),
        )
    }

    // In the order of the v1 settings, then the new ones
    private fun currentSettings(): List<Boolean> =
        listOf(
            settingsRepository.isVibrationAfterScanEnabled.value,
            settingsRepository.isOpenContentAfterScanEnabled.value,
            settingsRepository.isBeepAfterScanEnabled.value,
            settingsRepository.isCopyAfterScanEnabled.value,
            settingsRepository.isFrontCameraUsed.value,
            settingsRepository.isRawContentShown.value,
        )
}
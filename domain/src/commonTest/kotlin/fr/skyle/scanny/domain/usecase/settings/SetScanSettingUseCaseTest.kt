package fr.skyle.scanny.domain.usecase.settings

import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.model.ScanSetting
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetScanSettingUseCaseTest {

    private val settingsRepository = FakeSettingsRepository(
        isVibrationAfterScanEnabled = true,
        isOpenLinkAfterScanEnabled = false,
        isRawContentShown = false,
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
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the opening of a link after a scan changes alone`() = runTest {
        // When
        setScanSettingUseCase(
            setting = ScanSetting.OPEN_LINK_AFTER_SCAN,
            isEnabled = true,
        )

        // Then
        assertEquals(
            expected = listOf(
                true,
                true,
                false,
            ),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the display of the raw content changes alone`() = runTest {
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
                true,
            ),
            actual = currentSettings(),
        )
    }

    private fun currentSettings(): List<Boolean> =
        listOf(
            settingsRepository.isVibrationAfterScanEnabled.value,
            settingsRepository.isOpenLinkAfterScanEnabled.value,
            settingsRepository.isRawContentShown.value,
        )
}
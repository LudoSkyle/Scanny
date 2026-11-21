package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.model.HistorySetting
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetHistorySettingUseCaseTest {

    private val settingsRepository = FakeSettingsRepository()

    private val setHistorySettingUseCase = SetHistorySettingUseCaseImpl(settingsRepository = settingsRepository)

    @Test
    fun `the history of the scans changes alone`() = runTest {
        // When
        setHistorySettingUseCase(
            setting = HistorySetting.SCAN_HISTORY,
            isEnabled = false,
        )

        // Then
        assertEquals(
            expected = listOf(false, true, true),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the history of the creations changes alone`() = runTest {
        // When
        setHistorySettingUseCase(
            setting = HistorySetting.CREATION_HISTORY,
            isEnabled = false,
        )

        // Then
        assertEquals(
            expected = listOf(true, false, true),
            actual = currentSettings(),
        )
    }

    @Test
    fun `the skipping of the consecutive duplicates changes alone`() = runTest {
        // When
        setHistorySettingUseCase(
            setting = HistorySetting.CONSECUTIVE_DUPLICATE_SKIPPED,
            isEnabled = false,
        )

        // Then
        assertEquals(
            expected = listOf(true, true, false),
            actual = currentSettings(),
        )
    }

    private fun currentSettings(): List<Boolean> =
        listOf(
            settingsRepository.isScanHistoryEnabled.value,
            settingsRepository.isCreationHistoryEnabled.value,
            settingsRepository.isConsecutiveDuplicateSkipped.value,
        )
}
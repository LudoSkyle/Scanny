package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchHistorySettingsUseCaseTest {

    @Test
    fun `the settings of the history are read with their retention`() = runTest {
        // Given
        val watchHistorySettingsUseCase = WatchHistorySettingsUseCaseImpl(
            settingsRepository = FakeSettingsRepository(
                isScanHistoryEnabled = false,
                isCreationHistoryEnabled = true,
                isConsecutiveDuplicateSkipped = true,
                historyRetentionDays = 90,
            ),
        )

        // When
        val settings = watchHistorySettingsUseCase().first()

        // Then
        assertEquals(
            expected = HistorySettings(
                isScanHistoryEnabled = false,
                isCreationHistoryEnabled = true,
                isConsecutiveDuplicateSkipped = true,
                retention = HistoryRetention.THREE_MONTHS,
            ),
            actual = settings,
        )
    }

    @Test
    fun `no days is a retention forever`() = runTest {
        // Given
        val watchHistorySettingsUseCase = WatchHistorySettingsUseCaseImpl(
            settingsRepository = FakeSettingsRepository(historyRetentionDays = 0),
        )

        // When
        val settings = watchHistorySettingsUseCase().first()

        // Then
        assertEquals(
            expected = HistoryRetention.FOREVER,
            actual = settings.retention,
        )
    }

    @Test
    fun `a number of days no retention has falls back to a month`() = runTest {
        // Given
        val watchHistorySettingsUseCase = WatchHistorySettingsUseCaseImpl(
            settingsRepository = FakeSettingsRepository(historyRetentionDays = 12),
        )

        // When
        val settings = watchHistorySettingsUseCase().first()

        // Then
        assertEquals(
            expected = HistoryRetention.ONE_MONTH,
            actual = settings.retention,
        )
    }
}
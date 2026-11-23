package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.domain.fake.FakeCreationRepository
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.fake.FixedClock
import fr.skyle.scanny.domain.model.HistoryRetention
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class SetHistoryRetentionUseCaseTest {

    @Test
    fun `a new retention is saved and purges the older captures at once`() = runTest {
        // Given
        val scanRepository = FakeScanRepository()
        val creationRepository = FakeCreationRepository()
        val settingsRepository = FakeSettingsRepository(historyRetentionDays = 30)
        val setHistoryRetentionUseCase = SetHistoryRetentionUseCaseImpl(
            settingsRepository = settingsRepository,
            purgeHistoryUseCase = PurgeHistoryUseCaseImpl(
                scanRepository = scanRepository,
                creationRepository = creationRepository,
                settingsRepository = settingsRepository,
                clock = FixedClock(instant = NOW),
            ),
        )

        // When
        setHistoryRetentionUseCase(retention = HistoryRetention.ONE_WEEK)

        // Then
        assertEquals(
            expected = 7,
            actual = settingsRepository.historyRetentionDays.value,
        )
        assertEquals(
            expected = listOf<Instant?>(NOW - 7.days),
            actual = scanRepository.purges,
        )
    }

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.fake.FixedClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class PurgeHistoryUseCaseTest {

    private val scanRepository = FakeScanRepository()

    @Test
    fun `the purge deletes the captures older than the retention`() = runTest {
        // Given
        val purgeHistoryUseCase = purgeHistoryUseCase(settingsRepository = FakeSettingsRepository(historyRetentionDays = 30))

        // When
        purgeHistoryUseCase()

        // Then
        assertEquals(
            expected = listOf<Instant?>(NOW - 30.days),
            actual = scanRepository.purges,
        )
    }

    @Test
    fun `the purge keeps the whole history when it is kept forever`() = runTest {
        // Given
        val purgeHistoryUseCase = purgeHistoryUseCase(settingsRepository = FakeSettingsRepository(historyRetentionDays = 0))

        // When
        purgeHistoryUseCase()

        // Then
        assertEquals(
            expected = listOf<Instant?>(null),
            actual = scanRepository.purges,
        )
    }

    private fun purgeHistoryUseCase(settingsRepository: FakeSettingsRepository): PurgeHistoryUseCaseImpl =
        PurgeHistoryUseCaseImpl(
            scanRepository = scanRepository,
            settingsRepository = settingsRepository,
            clock = FixedClock(instant = NOW),
        )

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
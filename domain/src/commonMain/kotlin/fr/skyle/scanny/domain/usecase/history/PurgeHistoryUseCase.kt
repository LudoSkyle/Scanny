package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.HistoryRetention
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

interface PurgeHistoryUseCase {

    /**
     * Deletes the captures kept out of the history, shown once only, and those of the history older
     * than the retention the user chose.
     */
    suspend operator fun invoke()
}

internal class PurgeHistoryUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : PurgeHistoryUseCase {

    override suspend fun invoke() {
        val retention = HistoryRetention.fromDays(days = settingsRepository.watchHistoryRetentionDays().first())
        scanRepository.purgeScans(
            scannedBefore = retention.duration?.let { duration ->
                clock.now() - duration
            },
        )
    }
}
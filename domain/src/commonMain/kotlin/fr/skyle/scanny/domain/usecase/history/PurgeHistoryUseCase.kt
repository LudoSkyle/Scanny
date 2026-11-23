package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.data.repository.creation.api.CreationRepository
import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.HistoryRetention
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

interface PurgeHistoryUseCase {

    /**
     * Deletes the captures and the creations kept out of the history, shown once only, and those of
     * the history older than the retention the user chose.
     */
    suspend operator fun invoke()
}

internal class PurgeHistoryUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val creationRepository: CreationRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : PurgeHistoryUseCase {

    override suspend fun invoke() {
        val retention = HistoryRetention.fromDays(days = settingsRepository.watchHistoryRetentionDays().first())
        val before = retention.duration?.let { duration ->
            clock.now() - duration
        }
        scanRepository.purgeScans(scannedBefore = before)
        creationRepository.purgeCreations(createdBefore = before)
    }
}
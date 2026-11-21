package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.model.HistorySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface WatchHistorySettingsUseCase {

    operator fun invoke(): Flow<HistorySettings>
}

internal class WatchHistorySettingsUseCaseImpl(
    private val settingsRepository: SettingsRepository,
) : WatchHistorySettingsUseCase {

    override fun invoke(): Flow<HistorySettings> =
        combine(
            settingsRepository.watchIsScanHistoryEnabled(),
            settingsRepository.watchIsCreationHistoryEnabled(),
            settingsRepository.watchIsConsecutiveDuplicateSkipped(),
            settingsRepository.watchHistoryRetentionDays(),
        ) { isScanHistoryEnabled, isCreationHistoryEnabled, isConsecutiveDuplicateSkipped, retentionDays ->
            HistorySettings(
                isScanHistoryEnabled = isScanHistoryEnabled,
                isCreationHistoryEnabled = isCreationHistoryEnabled,
                isConsecutiveDuplicateSkipped = isConsecutiveDuplicateSkipped,
                retention = HistoryRetention.fromDays(days = retentionDays),
            )
        }
}
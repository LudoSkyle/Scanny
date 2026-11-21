package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.HistoryRetention

interface SetHistoryRetentionUseCase {

    /** Keeps the captures for [retention] from now on, and deletes at once those older than it. */
    suspend operator fun invoke(retention: HistoryRetention)
}

internal class SetHistoryRetentionUseCaseImpl(
    private val settingsRepository: SettingsRepository,
    private val purgeHistoryUseCase: PurgeHistoryUseCase,
) : SetHistoryRetentionUseCase {

    override suspend fun invoke(retention: HistoryRetention) {
        settingsRepository.setHistoryRetentionDays(days = retention.days)
        purgeHistoryUseCase()
    }
}
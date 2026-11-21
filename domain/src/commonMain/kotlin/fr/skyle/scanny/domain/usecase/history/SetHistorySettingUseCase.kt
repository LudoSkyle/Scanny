package fr.skyle.scanny.domain.usecase.history

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.HistorySetting

interface SetHistorySettingUseCase {

    suspend operator fun invoke(
        setting: HistorySetting,
        isEnabled: Boolean,
    )
}

internal class SetHistorySettingUseCaseImpl(
    private val settingsRepository: SettingsRepository,
) : SetHistorySettingUseCase {

    override suspend fun invoke(
        setting: HistorySetting,
        isEnabled: Boolean,
    ) {
        when (setting) {
            HistorySetting.SCAN_HISTORY -> settingsRepository.setScanHistoryEnabled(isEnabled = isEnabled)
            HistorySetting.CREATION_HISTORY -> settingsRepository.setCreationHistoryEnabled(isEnabled = isEnabled)
            HistorySetting.CONSECUTIVE_DUPLICATE_SKIPPED -> settingsRepository.setConsecutiveDuplicateSkipped(isSkipped = isEnabled)
        }
    }
}
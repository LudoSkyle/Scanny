package fr.skyle.scanny.domain.usecase.settings

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.ScanSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface WatchScanSettingsUseCase {

    operator fun invoke(): Flow<ScanSettings>
}

internal class WatchScanSettingsUseCaseImpl(
    private val settingsRepository: SettingsRepository,
) : WatchScanSettingsUseCase {

    override fun invoke(): Flow<ScanSettings> =
        combine(
            settingsRepository.watchIsVibrationAfterScanEnabled(),
            settingsRepository.watchIsOpenLinkAfterScanEnabled(),
            settingsRepository.watchIsRawContentShown(),
        ) { isVibrationAfterScanEnabled, isOpenLinkAfterScanEnabled, isRawContentShown ->
            ScanSettings(
                isVibrationAfterScanEnabled = isVibrationAfterScanEnabled,
                isOpenLinkAfterScanEnabled = isOpenLinkAfterScanEnabled,
                isRawContentShown = isRawContentShown,
            )
        }
}
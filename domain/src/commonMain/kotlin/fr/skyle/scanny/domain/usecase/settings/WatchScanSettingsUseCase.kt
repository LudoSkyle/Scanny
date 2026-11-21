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
            settingsRepository.watchIsBeepAfterScanEnabled(),
            settingsRepository.watchIsCopyAfterScanEnabled(),
            settingsRepository.watchIsOpenContentAfterScanEnabled(),
            settingsRepository.watchIsRawContentShown(),
        ) { isVibrationAfterScanEnabled, isBeepAfterScanEnabled, isCopyAfterScanEnabled, isOpenContentAfterScanEnabled, isRawContentShown ->
            ScanSettings(
                isVibrationAfterScanEnabled = isVibrationAfterScanEnabled,
                isBeepAfterScanEnabled = isBeepAfterScanEnabled,
                isCopyAfterScanEnabled = isCopyAfterScanEnabled,
                isOpenContentAfterScanEnabled = isOpenContentAfterScanEnabled,
                isRawContentShown = isRawContentShown,
            )
        }
}
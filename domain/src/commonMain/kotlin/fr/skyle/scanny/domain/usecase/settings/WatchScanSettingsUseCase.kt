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
        // More flows than a typed combine takes: their values come in the order of the flows
        combine(
            settingsRepository.watchIsVibrationAfterScanEnabled(),
            settingsRepository.watchIsBeepAfterScanEnabled(),
            settingsRepository.watchIsCopyAfterScanEnabled(),
            settingsRepository.watchIsOpenContentAfterScanEnabled(),
            settingsRepository.watchIsRawContentShown(),
            settingsRepository.watchIsFrontCameraUsed(),
        ) { values ->
            ScanSettings(
                isVibrationAfterScanEnabled = values[0],
                isBeepAfterScanEnabled = values[1],
                isCopyAfterScanEnabled = values[2],
                isOpenContentAfterScanEnabled = values[3],
                isRawContentShown = values[4],
                isFrontCameraUsed = values[5],
            )
        }
}
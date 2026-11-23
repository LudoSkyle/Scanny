package fr.skyle.scanny.domain.usecase.settings

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.model.ScanSetting

interface SetScanSettingUseCase {

    suspend operator fun invoke(
        setting: ScanSetting,
        isEnabled: Boolean,
    )
}

internal class SetScanSettingUseCaseImpl(
    private val settingsRepository: SettingsRepository,
) : SetScanSettingUseCase {

    override suspend fun invoke(
        setting: ScanSetting,
        isEnabled: Boolean,
    ) {
        when (setting) {
            ScanSetting.VIBRATION_AFTER_SCAN -> settingsRepository.setVibrationAfterScanEnabled(isEnabled = isEnabled)
            ScanSetting.BEEP_AFTER_SCAN -> settingsRepository.setBeepAfterScanEnabled(isEnabled = isEnabled)
            ScanSetting.COPY_AFTER_SCAN -> settingsRepository.setCopyAfterScanEnabled(isEnabled = isEnabled)
            ScanSetting.OPEN_CONTENT_AFTER_SCAN -> settingsRepository.setOpenContentAfterScanEnabled(isEnabled = isEnabled)
            ScanSetting.FRONT_CAMERA -> settingsRepository.setFrontCameraUsed(isUsed = isEnabled)
            ScanSetting.RAW_CONTENT_SHOWN -> settingsRepository.setRawContentShown(isShown = isEnabled)
        }
    }
}
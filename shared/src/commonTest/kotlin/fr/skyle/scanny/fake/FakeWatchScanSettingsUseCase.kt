package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.ScanSettings
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchScanSettingsUseCase(
    isVibrationAfterScanEnabled: Boolean = true,
    isOpenLinkAfterScanEnabled: Boolean = false,
    isRawContentShown: Boolean = false,
) : WatchScanSettingsUseCase {

    val settings = MutableStateFlow(
        ScanSettings(
            isVibrationAfterScanEnabled = isVibrationAfterScanEnabled,
            isOpenLinkAfterScanEnabled = isOpenLinkAfterScanEnabled,
            isRawContentShown = isRawContentShown,
        ),
    )

    override fun invoke(): Flow<ScanSettings> =
        settings
}
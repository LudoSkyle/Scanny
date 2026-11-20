package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.ScanSetting
import fr.skyle.scanny.domain.usecase.settings.SetScanSettingUseCase

/** Records the settings it is given, or fails if [error] is set. */
internal class FakeSetScanSettingUseCase(
    private val error: Exception? = null,
) : SetScanSettingUseCase {

    val savedSettings = mutableListOf<Pair<ScanSetting, Boolean>>()

    override suspend fun invoke(
        setting: ScanSetting,
        isEnabled: Boolean,
    ) {
        error?.let { throw it }

        savedSettings += setting to isEnabled
    }
}
package fr.skyle.scanny.domain.fake

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSettingsRepository(
    isVibrationAfterScanEnabled: Boolean = true,
    isOpenLinkAfterScanEnabled: Boolean = false,
    isRawContentShown: Boolean = false,
) : SettingsRepository {

    val isVibrationAfterScanEnabled = MutableStateFlow(isVibrationAfterScanEnabled)
    val isOpenLinkAfterScanEnabled = MutableStateFlow(isOpenLinkAfterScanEnabled)
    val isRawContentShown = MutableStateFlow(isRawContentShown)

    override fun watchIsVibrationAfterScanEnabled(): Flow<Boolean> =
        isVibrationAfterScanEnabled

    override suspend fun setVibrationAfterScanEnabled(isEnabled: Boolean) {
        isVibrationAfterScanEnabled.value = isEnabled
    }

    override fun watchIsOpenLinkAfterScanEnabled(): Flow<Boolean> =
        isOpenLinkAfterScanEnabled

    override suspend fun setOpenLinkAfterScanEnabled(isEnabled: Boolean) {
        isOpenLinkAfterScanEnabled.value = isEnabled
    }

    override fun watchIsRawContentShown(): Flow<Boolean> =
        isRawContentShown

    override suspend fun setRawContentShown(isShown: Boolean) {
        isRawContentShown.value = isShown
    }
}
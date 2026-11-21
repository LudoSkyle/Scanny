package fr.skyle.scanny.domain.fake

import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSettingsRepository(
    isVibrationAfterScanEnabled: Boolean = true,
    isBeepAfterScanEnabled: Boolean = false,
    isCopyAfterScanEnabled: Boolean = false,
    isOpenContentAfterScanEnabled: Boolean = false,
    isRawContentShown: Boolean = false,
    isScanHistoryEnabled: Boolean = true,
    isConsecutiveDuplicateSkipped: Boolean = true,
    historyRetentionDays: Int = 30,
) : SettingsRepository {

    val isVibrationAfterScanEnabled = MutableStateFlow(isVibrationAfterScanEnabled)
    val isBeepAfterScanEnabled = MutableStateFlow(isBeepAfterScanEnabled)
    val isCopyAfterScanEnabled = MutableStateFlow(isCopyAfterScanEnabled)
    val isOpenContentAfterScanEnabled = MutableStateFlow(isOpenContentAfterScanEnabled)
    val isRawContentShown = MutableStateFlow(isRawContentShown)
    val isScanHistoryEnabled = MutableStateFlow(isScanHistoryEnabled)
    val isConsecutiveDuplicateSkipped = MutableStateFlow(isConsecutiveDuplicateSkipped)
    val historyRetentionDays = MutableStateFlow(historyRetentionDays)

    override fun watchIsVibrationAfterScanEnabled(): Flow<Boolean> =
        isVibrationAfterScanEnabled

    override suspend fun setVibrationAfterScanEnabled(isEnabled: Boolean) {
        isVibrationAfterScanEnabled.value = isEnabled
    }

    override fun watchIsBeepAfterScanEnabled(): Flow<Boolean> =
        isBeepAfterScanEnabled

    override suspend fun setBeepAfterScanEnabled(isEnabled: Boolean) {
        isBeepAfterScanEnabled.value = isEnabled
    }

    override fun watchIsCopyAfterScanEnabled(): Flow<Boolean> =
        isCopyAfterScanEnabled

    override suspend fun setCopyAfterScanEnabled(isEnabled: Boolean) {
        isCopyAfterScanEnabled.value = isEnabled
    }

    override fun watchIsOpenContentAfterScanEnabled(): Flow<Boolean> =
        isOpenContentAfterScanEnabled

    override suspend fun setOpenContentAfterScanEnabled(isEnabled: Boolean) {
        isOpenContentAfterScanEnabled.value = isEnabled
    }

    override fun watchIsRawContentShown(): Flow<Boolean> =
        isRawContentShown

    override suspend fun setRawContentShown(isShown: Boolean) {
        isRawContentShown.value = isShown
    }

    override fun watchIsScanHistoryEnabled(): Flow<Boolean> =
        isScanHistoryEnabled

    override suspend fun setScanHistoryEnabled(isEnabled: Boolean) {
        isScanHistoryEnabled.value = isEnabled
    }

    override fun watchIsConsecutiveDuplicateSkipped(): Flow<Boolean> =
        isConsecutiveDuplicateSkipped

    override suspend fun setConsecutiveDuplicateSkipped(isSkipped: Boolean) {
        isConsecutiveDuplicateSkipped.value = isSkipped
    }

    override fun watchHistoryRetentionDays(): Flow<Int> =
        historyRetentionDays

    override suspend fun setHistoryRetentionDays(days: Int) {
        historyRetentionDays.value = days
    }
}
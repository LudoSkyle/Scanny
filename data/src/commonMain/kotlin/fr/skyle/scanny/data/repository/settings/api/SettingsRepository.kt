package fr.skyle.scanny.data.repository.settings.api

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun watchIsVibrationAfterScanEnabled(): Flow<Boolean>

    suspend fun setVibrationAfterScanEnabled(isEnabled: Boolean)

    fun watchIsBeepAfterScanEnabled(): Flow<Boolean>

    suspend fun setBeepAfterScanEnabled(isEnabled: Boolean)

    fun watchIsCopyAfterScanEnabled(): Flow<Boolean>

    suspend fun setCopyAfterScanEnabled(isEnabled: Boolean)

    fun watchIsOpenContentAfterScanEnabled(): Flow<Boolean>

    suspend fun setOpenContentAfterScanEnabled(isEnabled: Boolean)

    fun watchIsRawContentShown(): Flow<Boolean>

    suspend fun setRawContentShown(isShown: Boolean)

    fun watchIsFrontCameraUsed(): Flow<Boolean>

    suspend fun setFrontCameraUsed(isUsed: Boolean)

    fun watchIsScanHistoryEnabled(): Flow<Boolean>

    suspend fun setScanHistoryEnabled(isEnabled: Boolean)

    fun watchIsConsecutiveDuplicateSkipped(): Flow<Boolean>

    suspend fun setConsecutiveDuplicateSkipped(isSkipped: Boolean)

    /** How many days a capture stays in the history; 0 when it stays forever. */
    fun watchHistoryRetentionDays(): Flow<Int>

    suspend fun setHistoryRetentionDays(days: Int)
}
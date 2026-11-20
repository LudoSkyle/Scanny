package fr.skyle.scanny.data.repository.settings.api

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun watchIsVibrationAfterScanEnabled(): Flow<Boolean>

    suspend fun setVibrationAfterScanEnabled(isEnabled: Boolean)

    fun watchIsOpenLinkAfterScanEnabled(): Flow<Boolean>

    suspend fun setOpenLinkAfterScanEnabled(isEnabled: Boolean)

    fun watchIsRawContentShown(): Flow<Boolean>

    suspend fun setRawContentShown(isShown: Boolean)
}
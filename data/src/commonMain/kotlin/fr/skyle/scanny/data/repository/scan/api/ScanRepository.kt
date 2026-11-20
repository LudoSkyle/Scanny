package fr.skyle.scanny.data.repository.scan.api

import fr.skyle.scanny.data.model.StoredScan
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface ScanRepository {

    suspend fun saveScan(
        rawValue: String,
        format: String,
        scannedAt: Instant,
    ): Long

    fun watchScans(): Flow<List<StoredScan>>

    fun watchScan(id: Long): Flow<StoredScan?>

    suspend fun deleteScan(id: Long)
}
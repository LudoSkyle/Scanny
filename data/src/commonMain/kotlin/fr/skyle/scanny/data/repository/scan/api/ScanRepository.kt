package fr.skyle.scanny.data.repository.scan.api

import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface ScanRepository {

    /** Records a capture of [codes], at least one, in their order, and returns its id. */
    suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
    ): Long

    fun watchScans(): Flow<List<StoredScan>>

    fun watchScan(id: Long): Flow<StoredScan?>

    fun watchCode(id: Long): Flow<StoredCode?>

    suspend fun deleteScan(id: Long)

    suspend fun deleteAllScans()

    /** Deletes a code, and its capture when it was the last code of it. */
    suspend fun deleteCode(id: Long)
}
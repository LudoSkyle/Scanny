package fr.skyle.scanny.data.repository.scan.api

import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface ScanRepository {

    /**
     * Records a capture of [codes], at least one, in their order, and returns its id. Out of the
     * history ([isInHistory] false), it can still be watched by its id until [purgeScans].
     */
    suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
        isInHistory: Boolean,
    ): Long

    /** The captures of the history, its favorites included whatever its setting, from the newest to the oldest. */
    fun watchScans(): Flow<List<StoredScan>>

    /** The newest capture of the history, null when it is empty. */
    suspend fun findLatestScan(): StoredScan?

    /** Gives a capture a new time, as if it was just recorded. */
    suspend fun updateScanTime(
        id: Long,
        scannedAt: Instant,
    )

    /** Puts a capture among the favorites, or takes it out: a favorite is never purged. */
    suspend fun setScanFavorite(
        id: Long,
        isFavorite: Boolean,
    )

    fun watchScan(id: Long): Flow<StoredScan?>

    fun watchCode(id: Long): Flow<StoredCode?>

    suspend fun deleteScan(id: Long)

    suspend fun deleteAllScans()

    /**
     * Deletes the captures out of the history, and those of the history older than [scannedBefore], if
     * set: all but the favorites.
     */
    suspend fun purgeScans(scannedBefore: Instant?)

    /** Deletes a code, and its capture when it was the last code of it. */
    suspend fun deleteCode(id: Long)
}
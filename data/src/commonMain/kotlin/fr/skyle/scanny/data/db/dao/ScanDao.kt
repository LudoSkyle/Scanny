package fr.skyle.scanny.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity
import fr.skyle.scanny.data.db.relation.ScanWithCodes
import fr.skyle.scanny.data.db.relation.ScannedCodeWithScanTime
import kotlinx.coroutines.flow.Flow

// An abstract class and not an interface: Room only runs a method with a body in a transaction there
@Dao
internal abstract class ScanDao {

    @Insert
    abstract suspend fun insert(entity: ScanEntity): Long

    @Insert
    abstract suspend fun insertCodes(entities: List<ScannedCodeEntity>)

    @Transaction
    open suspend fun insertWithCodes(
        scan: ScanEntity,
        codes: List<ScannedCodeEntity>,
    ): Long {
        val scanId = insert(entity = scan)
        insertCodes(
            entities = codes.map { code ->
                code.copy(scanId = scanId)
            },
        )
        return scanId
    }

    // The id breaks the tie between two captures of the same millisecond: the last recorded comes first
    @Transaction
    @Query("SELECT * FROM scan ORDER BY scanned_at DESC, id DESC")
    abstract fun watchAll(): Flow<List<ScanWithCodes>>

    @Transaction
    @Query("SELECT * FROM scan WHERE id = :id")
    abstract fun watchById(id: Long): Flow<ScanWithCodes?>

    @Query(
        "SELECT scanned_code.*, scan.scanned_at FROM scanned_code " +
            "INNER JOIN scan ON scan.id = scanned_code.scan_id WHERE scanned_code.id = :id",
    )
    abstract fun watchCodeById(id: Long): Flow<ScannedCodeWithScanTime?>

    // Its codes go with it, through the cascade of their foreign key
    @Query("DELETE FROM scan WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    // The codes go with their captures, through the cascade of their foreign key
    @Query("DELETE FROM scan")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM scanned_code WHERE id = :id")
    abstract suspend fun deleteCode(id: Long)

    @Query("DELETE FROM scan WHERE id NOT IN (SELECT scan_id FROM scanned_code)")
    abstract suspend fun deleteScansWithoutCode()

    // A capture never stays without code
    @Transaction
    open suspend fun deleteCodeAndEmptyScan(id: Long) {
        deleteCode(id = id)
        deleteScansWithoutCode()
    }
}
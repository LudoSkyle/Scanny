package fr.skyle.scanny.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.skyle.scanny.data.db.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ScanDao {

    @Insert
    suspend fun insert(entity: ScanEntity): Long

    // The id breaks the tie between two scans of the same millisecond: the last recorded comes first
    @Query("SELECT * FROM scan ORDER BY scanned_at DESC, id DESC")
    fun watchAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scan WHERE id = :id")
    fun watchById(id: Long): Flow<ScanEntity?>

    @Query("DELETE FROM scan WHERE id = :id")
    suspend fun deleteById(id: Long)
}
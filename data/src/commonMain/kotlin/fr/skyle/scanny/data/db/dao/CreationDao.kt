package fr.skyle.scanny.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.skyle.scanny.data.db.entity.CreationEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CreationDao {

    @Insert
    suspend fun insert(entity: CreationEntity): Long

    // The id breaks the tie between two creations of the same millisecond: the last one comes first
    @Query("SELECT * FROM creation WHERE is_in_history = 1 ORDER BY created_at DESC, id DESC")
    fun watchHistory(): Flow<List<CreationEntity>>

    @Query("SELECT * FROM creation WHERE id = :id")
    fun watchById(id: Long): Flow<CreationEntity?>

    @Query("DELETE FROM creation WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM creation")
    suspend fun deleteAll()

    @Query("DELETE FROM creation WHERE is_in_history = 0")
    suspend fun deleteOutOfHistory()

    @Query("DELETE FROM creation WHERE created_at < :before")
    suspend fun deleteCreatedBefore(before: Long)
}
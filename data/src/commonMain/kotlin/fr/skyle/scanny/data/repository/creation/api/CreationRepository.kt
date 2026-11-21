package fr.skyle.scanny.data.repository.creation.api

import fr.skyle.scanny.data.model.StoredCreation
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface CreationRepository {

    /**
     * Records a creation and returns its id. Out of the history ([isInHistory] false), it can still be
     * watched by its id until [purgeCreations].
     */
    suspend fun saveCreation(
        rawValue: String,
        format: String,
        createdAt: Instant,
        isInHistory: Boolean,
    ): Long

    /** The creations of the history, from the newest to the oldest. */
    fun watchCreations(): Flow<List<StoredCreation>>

    fun watchCreation(id: Long): Flow<StoredCreation?>

    suspend fun deleteCreation(id: Long)

    suspend fun deleteAllCreations()

    /** Deletes the creations out of the history, and those of the history older than [createdBefore], if set. */
    suspend fun purgeCreations(createdBefore: Instant?)
}
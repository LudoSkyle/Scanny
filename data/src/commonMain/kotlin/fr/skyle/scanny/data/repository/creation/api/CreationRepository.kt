package fr.skyle.scanny.data.repository.creation.api

import fr.skyle.scanny.data.model.StoredCodeStyle
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
        style: StoredCodeStyle,
        createdAt: Instant,
        isInHistory: Boolean,
    ): Long

    /** The creations of the history, its favorites included whatever its setting, from the newest to the oldest. */
    fun watchCreations(): Flow<List<StoredCreation>>

    fun watchCreation(id: Long): Flow<StoredCreation?>

    /** Puts a creation among the favorites, or takes it out: a favorite is never purged. */
    suspend fun setCreationFavorite(
        id: Long,
        isFavorite: Boolean,
    )

    suspend fun deleteCreation(id: Long)

    suspend fun deleteAllCreations()

    /**
     * Deletes the creations out of the history, and those of the history older than [createdBefore], if
     * set: all but the favorites.
     */
    suspend fun purgeCreations(createdBefore: Instant?)
}
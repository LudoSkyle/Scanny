package fr.skyle.scanny.domain.fake

import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.data.model.StoredCreation
import fr.skyle.scanny.data.repository.creation.api.CreationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

internal class FakeCreationRepository(
    storedCreations: List<StoredCreation> = emptyList(),
) : CreationRepository {

    private val creations = MutableStateFlow(storedCreations)

    val storedCreations: List<StoredCreation>
        get() = creations.value

    // The ids of the creations recorded out of the history
    val outOfHistoryIds = mutableSetOf<Long>()

    // The time before which each purge deleted the creations, null for none
    val purges = mutableListOf<Instant?>()

    override suspend fun saveCreation(
        rawValue: String,
        format: String,
        style: StoredCodeStyle,
        createdAt: Instant,
        isInHistory: Boolean,
    ): Long {
        val creationId = (creations.value.maxOfOrNull { it.id } ?: 0) + 1
        creations.update { storedCreations ->
            storedCreations + StoredCreation(
                id = creationId,
                rawValue = rawValue,
                format = format,
                style = style,
                createdAt = createdAt,
            )
        }
        if (!isInHistory) {
            outOfHistoryIds += creationId
        }
        return creationId
    }

    override fun watchCreations(): Flow<List<StoredCreation>> =
        creations.map { storedCreations ->
            storedCreations.filterNot { it.id in outOfHistoryIds }
        }

    override fun watchCreation(id: Long): Flow<StoredCreation?> =
        creations.map { storedCreations ->
            storedCreations.firstOrNull { it.id == id }
        }

    override suspend fun deleteCreation(id: Long) {
        creations.update { storedCreations ->
            storedCreations.filterNot { it.id == id }
        }
    }

    override suspend fun deleteAllCreations() {
        creations.value = emptyList()
    }

    override suspend fun purgeCreations(createdBefore: Instant?) {
        purges += createdBefore
        creations.update { storedCreations ->
            storedCreations.filterNot { storedCreation ->
                storedCreation.id in outOfHistoryIds || (createdBefore != null && storedCreation.createdAt < createdBefore)
            }
        }
    }
}
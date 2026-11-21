package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.creation.DeleteCreationUseCase

/** Records the creations deleted, or fails if [error] is set. */
internal class FakeDeleteCreationUseCase(
    private val error: Exception? = null,
) : DeleteCreationUseCase {

    val deletedCreationIds = mutableListOf<Long>()

    override suspend fun invoke(creationId: Long) {
        error?.let { throw it }

        deletedCreationIds += creationId
    }
}
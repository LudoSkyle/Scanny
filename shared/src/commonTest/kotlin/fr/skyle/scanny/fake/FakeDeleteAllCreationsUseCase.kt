package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.creation.DeleteAllCreationsUseCase

/** Counts the times the creations are all deleted, or fails if [error] is set. */
internal class FakeDeleteAllCreationsUseCase(
    private val error: Exception? = null,
) : DeleteAllCreationsUseCase {

    var clearCount = 0
        private set

    override suspend fun invoke() {
        error?.let { throw it }

        clearCount += 1
    }
}

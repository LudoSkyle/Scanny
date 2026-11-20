package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.scan.DeleteAllScansUseCase

/** Counts the times the history is cleared, or fails if [error] is set. */
internal class FakeDeleteAllScansUseCase(
    private val error: Exception? = null,
) : DeleteAllScansUseCase {

    var clearCount = 0
        private set

    override suspend fun invoke() {
        error?.let { throw it }

        clearCount += 1
    }
}
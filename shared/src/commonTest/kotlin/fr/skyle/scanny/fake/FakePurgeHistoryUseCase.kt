package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.history.PurgeHistoryUseCase

/** Counts the purges, or fails if [error] is set. */
internal class FakePurgeHistoryUseCase(
    private val error: Exception? = null,
) : PurgeHistoryUseCase {

    var purgeCount = 0
        private set

    override suspend fun invoke() {
        error?.let { throw it }

        purgeCount += 1
    }
}
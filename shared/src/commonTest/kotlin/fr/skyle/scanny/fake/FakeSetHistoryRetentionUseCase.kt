package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.domain.usecase.history.SetHistoryRetentionUseCase

/** Records the retentions it is given, or fails if [error] is set. */
internal class FakeSetHistoryRetentionUseCase(
    private val error: Exception? = null,
) : SetHistoryRetentionUseCase {

    val savedRetentions = mutableListOf<HistoryRetention>()

    override suspend fun invoke(retention: HistoryRetention) {
        error?.let { throw it }

        savedRetentions += retention
    }
}
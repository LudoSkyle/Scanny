package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.scan.SetScanFavoriteUseCase

/** Records the favorites it sets, by capture, or fails if [error] is set. */
internal class FakeSetScanFavoriteUseCase(
    private val error: Exception? = null,
) : SetScanFavoriteUseCase {

    val setFavorites = mutableListOf<Pair<Long, Boolean>>()

    override suspend fun invoke(
        scanId: Long,
        isFavorite: Boolean,
    ) {
        error?.let { throw it }

        setFavorites += scanId to isFavorite
    }
}

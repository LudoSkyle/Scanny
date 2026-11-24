package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.creation.SetCreationFavoriteUseCase

/** Records the favorites it sets, by creation, or fails if [error] is set. */
internal class FakeSetCreationFavoriteUseCase(
    private val error: Exception? = null,
) : SetCreationFavoriteUseCase {

    val setFavorites = mutableListOf<Pair<Long, Boolean>>()

    override suspend fun invoke(
        creationId: Long,
        isFavorite: Boolean,
    ) {
        error?.let { throw it }

        setFavorites += creationId to isFavorite
    }
}

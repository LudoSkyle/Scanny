package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.repository.creation.api.CreationRepository

/** Puts a creation among the favorites, or takes it out. */
interface SetCreationFavoriteUseCase {

    suspend operator fun invoke(
        creationId: Long,
        isFavorite: Boolean,
    )
}

internal class SetCreationFavoriteUseCaseImpl(
    private val creationRepository: CreationRepository,
) : SetCreationFavoriteUseCase {

    override suspend fun invoke(
        creationId: Long,
        isFavorite: Boolean,
    ) {
        creationRepository.setCreationFavorite(
            id = creationId,
            isFavorite = isFavorite,
        )
    }
}

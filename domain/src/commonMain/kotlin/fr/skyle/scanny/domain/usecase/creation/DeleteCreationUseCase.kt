package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.repository.creation.api.CreationRepository

interface DeleteCreationUseCase {

    suspend operator fun invoke(creationId: Long)
}

internal class DeleteCreationUseCaseImpl(
    private val creationRepository: CreationRepository,
) : DeleteCreationUseCase {

    override suspend fun invoke(creationId: Long) {
        creationRepository.deleteCreation(id = creationId)
    }
}
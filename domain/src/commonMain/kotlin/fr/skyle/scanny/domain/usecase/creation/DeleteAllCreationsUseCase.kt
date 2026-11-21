package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.repository.creation.api.CreationRepository

interface DeleteAllCreationsUseCase {

    suspend operator fun invoke()
}

internal class DeleteAllCreationsUseCaseImpl(
    private val creationRepository: CreationRepository,
) : DeleteAllCreationsUseCase {

    override suspend fun invoke() {
        creationRepository.deleteAllCreations()
    }
}
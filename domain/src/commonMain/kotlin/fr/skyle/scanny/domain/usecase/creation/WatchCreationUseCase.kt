package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.repository.creation.api.CreationRepository
import fr.skyle.scanny.domain.mapper.toCreation
import fr.skyle.scanny.domain.model.Creation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface WatchCreationUseCase {

    operator fun invoke(creationId: Long): Flow<Creation?>
}

internal class WatchCreationUseCaseImpl(
    private val creationRepository: CreationRepository,
) : WatchCreationUseCase {

    override fun invoke(creationId: Long): Flow<Creation?> =
        creationRepository.watchCreation(id = creationId)
            .map { storedCreation ->
                storedCreation?.toCreation()
            }
            .flowOn(Dispatchers.Default)
}
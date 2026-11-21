package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.repository.creation.api.CreationRepository
import fr.skyle.scanny.domain.mapper.toCreation
import fr.skyle.scanny.domain.model.Creation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface WatchCreationsUseCase {

    /** The creations of the history, from the newest to the oldest. */
    operator fun invoke(): Flow<List<Creation>>
}

internal class WatchCreationsUseCaseImpl(
    private val creationRepository: CreationRepository,
) : WatchCreationsUseCase {

    override fun invoke(): Flow<List<Creation>> =
        creationRepository.watchCreations()
            .map { creations ->
                creations.map { creation ->
                    creation.toCreation()
                }
            }
            .flowOn(Dispatchers.Default)
}
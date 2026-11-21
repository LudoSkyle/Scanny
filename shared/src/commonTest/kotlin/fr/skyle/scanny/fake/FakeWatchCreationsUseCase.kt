package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.usecase.creation.WatchCreationsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchCreationsUseCase(
    creations: List<Creation> = emptyList(),
) : WatchCreationsUseCase {

    val creations = MutableStateFlow(creations)

    override fun invoke(): Flow<List<Creation>> =
        creations
}

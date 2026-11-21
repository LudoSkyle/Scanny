package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.usecase.creation.WatchCreationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchCreationUseCase(
    creation: Creation?,
) : WatchCreationUseCase {

    val creation = MutableStateFlow(creation)

    override fun invoke(creationId: Long): Flow<Creation?> =
        creation
}
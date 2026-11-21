package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.repository.creation.api.CreationRepository
import fr.skyle.scanny.data.repository.settings.api.SettingsRepository
import fr.skyle.scanny.domain.mapper.toCreation
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.payload.toRawValue
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

interface RecordCreationUseCase {

    /**
     * Records a code holding [content], in [format], and returns it. When the user keeps no history of
     * the creations, it is recorded out of it, then purged.
     */
    suspend operator fun invoke(
        content: BarcodeContent,
        format: BarcodeFormat,
    ): Creation
}

internal class RecordCreationUseCaseImpl(
    private val creationRepository: CreationRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : RecordCreationUseCase {

    override suspend fun invoke(
        content: BarcodeContent,
        format: BarcodeFormat,
    ): Creation {
        val creationId = creationRepository.saveCreation(
            rawValue = content.toRawValue(),
            format = format.name,
            createdAt = clock.now(),
            isInHistory = settingsRepository.watchIsCreationHistoryEnabled().first(),
        )

        return checkNotNull(creationRepository.watchCreation(id = creationId).first()) {
            "The creation $creationId was not found once recorded"
        }.toCreation()
    }
}
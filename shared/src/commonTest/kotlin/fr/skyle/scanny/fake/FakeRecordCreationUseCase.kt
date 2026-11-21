package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.payload.toRawValue
import fr.skyle.scanny.domain.usecase.creation.RecordCreationUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Instant

/**
 * Records the creations it is given, as the creation of id 1, 2…; fails if [error] is set, and waits
 * for [completion] when set.
 */
internal class FakeRecordCreationUseCase(
    private val error: Exception? = null,
    private val completion: CompletableDeferred<Unit>? = null,
) : RecordCreationUseCase {

    val recordedCreations = mutableListOf<Pair<BarcodeContent, BarcodeFormat>>()

    override suspend fun invoke(
        content: BarcodeContent,
        format: BarcodeFormat,
    ): Creation {
        error?.let { throw it }

        recordedCreations += content to format
        completion?.await()

        return Creation(
            id = recordedCreations.size.toLong(),
            content = content,
            rawValue = content.toRawValue(),
            format = format,
            createdAt = Instant.fromEpochMilliseconds(0),
        )
    }
}
package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
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

    val recordedCreations = mutableListOf<RecordedCreation>()

    override suspend fun invoke(
        content: BarcodeContent,
        format: BarcodeFormat,
        style: CodeStyle,
    ): Creation {
        error?.let { throw it }

        recordedCreations += RecordedCreation(
            content = content,
            format = format,
            style = style,
        )
        completion?.await()

        return Creation(
            id = recordedCreations.size.toLong(),
            content = content,
            rawValue = content.toRawValue(),
            format = format,
            style = style,
            createdAt = Instant.fromEpochMilliseconds(0),
        )
    }

    data class RecordedCreation(
        val content: BarcodeContent,
        val format: BarcodeFormat,
        val style: CodeStyle,
    )
}
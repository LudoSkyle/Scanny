package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.scan.DeleteScannedCodeUseCase

/** Records the codes it deletes, or fails if [error] is set. */
internal class FakeDeleteScannedCodeUseCase(
    private val error: Exception? = null,
) : DeleteScannedCodeUseCase {

    val deletedCodeIds = mutableListOf<Long>()

    override suspend fun invoke(codeId: Long) {
        error?.let { throw it }

        deletedCodeIds += codeId
    }
}
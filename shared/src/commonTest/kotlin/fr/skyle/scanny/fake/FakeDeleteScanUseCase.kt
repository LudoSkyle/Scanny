package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.usecase.scan.DeleteScanUseCase

/** Records the captures it deletes, or fails if [error] is set. */
internal class FakeDeleteScanUseCase(
    private val error: Exception? = null,
) : DeleteScanUseCase {

    val deletedScanIds = mutableListOf<Long>()

    override suspend fun invoke(scanId: Long) {
        error?.let { throw it }

        deletedScanIds += scanId
    }
}
package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository

interface DeleteAllScansUseCase {

    /** Clears the history of the scans. */
    suspend operator fun invoke()
}

internal class DeleteAllScansUseCaseImpl(
    private val scanRepository: ScanRepository,
) : DeleteAllScansUseCase {

    override suspend fun invoke() {
        scanRepository.deleteAllScans()
    }
}
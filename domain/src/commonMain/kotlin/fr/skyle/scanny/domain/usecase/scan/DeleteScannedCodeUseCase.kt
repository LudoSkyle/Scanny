package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository

interface DeleteScannedCodeUseCase {

    /** Deletes a code, and its capture when it was the last code of it. */
    suspend operator fun invoke(codeId: Long)
}

internal class DeleteScannedCodeUseCaseImpl(
    private val scanRepository: ScanRepository,
) : DeleteScannedCodeUseCase {

    override suspend fun invoke(codeId: Long) {
        scanRepository.deleteCode(id = codeId)
    }
}
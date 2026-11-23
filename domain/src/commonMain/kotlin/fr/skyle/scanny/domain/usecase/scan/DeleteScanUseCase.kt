package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository

interface DeleteScanUseCase {

    suspend operator fun invoke(scanId: Long)
}

internal class DeleteScanUseCaseImpl(
    private val scanRepository: ScanRepository,
) : DeleteScanUseCase {

    override suspend fun invoke(scanId: Long) {
        scanRepository.deleteScan(id = scanId)
    }
}
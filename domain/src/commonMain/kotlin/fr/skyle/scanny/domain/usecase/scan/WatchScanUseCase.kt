package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.domain.mapper.toScan
import fr.skyle.scanny.domain.model.Scan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface WatchScanUseCase {

    operator fun invoke(scanId: Long): Flow<Scan?>
}

internal class WatchScanUseCaseImpl(
    private val scanRepository: ScanRepository,
) : WatchScanUseCase {

    override fun invoke(scanId: Long): Flow<Scan?> =
        scanRepository.watchScan(id = scanId)
            .map { storedScan ->
                storedScan?.toScan()
            }
            .flowOn(Dispatchers.Default)
}
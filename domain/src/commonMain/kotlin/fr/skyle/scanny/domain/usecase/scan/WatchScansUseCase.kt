package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.domain.mapper.toScan
import fr.skyle.scanny.domain.model.Scan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface WatchScansUseCase {

    operator fun invoke(): Flow<List<Scan>>
}

internal class WatchScansUseCaseImpl(
    private val scanRepository: ScanRepository,
) : WatchScansUseCase {

    override fun invoke(): Flow<List<Scan>> =
        scanRepository.watchScans()
            .map { storedScans ->
                storedScans.map { storedScan ->
                    storedScan.toScan()
                }
            }
            .flowOn(Dispatchers.Default)
}
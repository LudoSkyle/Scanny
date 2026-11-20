package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.usecase.scan.WatchScanUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchScanUseCase(
    scan: Scan?,
) : WatchScanUseCase {

    val scan = MutableStateFlow(scan)

    val watchedScanIds = mutableListOf<Long>()

    override fun invoke(scanId: Long): Flow<Scan?> {
        watchedScanIds += scanId
        return scan
    }
}
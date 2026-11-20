package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchScansUseCase(
    scans: List<Scan> = emptyList(),
) : WatchScansUseCase {

    val scans = MutableStateFlow(scans)

    override fun invoke(): Flow<List<Scan>> =
        scans
}
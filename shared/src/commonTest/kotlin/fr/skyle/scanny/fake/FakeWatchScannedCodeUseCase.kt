package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.usecase.scan.WatchScannedCodeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWatchScannedCodeUseCase(
    scannedCode: ScannedCode?,
) : WatchScannedCodeUseCase {

    val scannedCode = MutableStateFlow(scannedCode)

    val watchedCodeIds = mutableListOf<Long>()

    override fun invoke(codeId: Long): Flow<ScannedCode?> {
        watchedCodeIds += codeId
        return scannedCode
    }
}
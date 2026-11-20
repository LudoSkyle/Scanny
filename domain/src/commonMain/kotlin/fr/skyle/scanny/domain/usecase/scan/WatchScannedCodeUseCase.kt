package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import fr.skyle.scanny.domain.mapper.toScannedCode
import fr.skyle.scanny.domain.model.ScannedCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface WatchScannedCodeUseCase {

    operator fun invoke(codeId: Long): Flow<ScannedCode?>
}

internal class WatchScannedCodeUseCaseImpl(
    private val scanRepository: ScanRepository,
) : WatchScannedCodeUseCase {

    override fun invoke(codeId: Long): Flow<ScannedCode?> =
        scanRepository.watchCode(id = codeId)
            .map { storedCode ->
                storedCode?.toScannedCode()
            }
            .flowOn(Dispatchers.Default)
}
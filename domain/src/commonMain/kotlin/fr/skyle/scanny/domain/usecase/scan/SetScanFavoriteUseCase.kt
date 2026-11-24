package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.repository.scan.api.ScanRepository

/** Puts a capture among the favorites, or takes it out: a group is one as a whole. */
interface SetScanFavoriteUseCase {

    suspend operator fun invoke(
        scanId: Long,
        isFavorite: Boolean,
    )
}

internal class SetScanFavoriteUseCaseImpl(
    private val scanRepository: ScanRepository,
) : SetScanFavoriteUseCase {

    override suspend fun invoke(
        scanId: Long,
        isFavorite: Boolean,
    ) {
        scanRepository.setScanFavorite(
            id = scanId,
            isFavorite = isFavorite,
        )
    }
}

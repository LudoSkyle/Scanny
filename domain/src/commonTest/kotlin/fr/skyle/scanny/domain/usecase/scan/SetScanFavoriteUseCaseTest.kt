package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.domain.fake.FakeScanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SetScanFavoriteUseCaseTest {

    @Test
    fun `a capture is put among the favorites and taken out`() = runTest {
        // Given
        val scanRepository = FakeScanRepository()
        val scanId = scanRepository.saveScan(
            codes = listOf(CodeToSave(rawValue = "https://scanny.app", format = "QR_CODE")),
            scannedAt = Instant.fromEpochMilliseconds(1_000),
            isInHistory = true,
        )
        val useCase = SetScanFavoriteUseCaseImpl(scanRepository = scanRepository)

        // When
        useCase(scanId = scanId, isFavorite = true)
        val favorite = scanRepository.watchScan(id = scanId).first()?.isFavorite
        useCase(scanId = scanId, isFavorite = false)

        // Then
        assertEquals(
            expected = true to false,
            actual = favorite to scanRepository.watchScan(id = scanId).first()?.isFavorite,
        )
    }
}

package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DeleteAllScansUseCaseTest {

    @Test
    fun `clearing the history deletes every scan`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(id = 1),
                storedScan(id = 2),
            ),
        )
        val deleteAllScansUseCase = DeleteAllScansUseCaseImpl(scanRepository = scanRepository)

        // When
        deleteAllScansUseCase()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = scanRepository.storedScans,
        )
    }

    private fun storedScan(id: Long): StoredScan =
        StoredScan(
            id = id,
            scannedAt = SCANNED_AT,
            codes = listOf(
                StoredCode(
                    id = id,
                    scanId = id,
                    rawValue = "https://scanny.app",
                    format = "QR_CODE",
                    scannedAt = SCANNED_AT,
                ),
            ),
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(0)
    }
}
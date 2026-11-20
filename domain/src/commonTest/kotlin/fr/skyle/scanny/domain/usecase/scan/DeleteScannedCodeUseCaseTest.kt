package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DeleteScannedCodeUseCaseTest {

    @Test
    fun `the code is deleted and the other codes of its scan are kept`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                StoredScan(
                    id = 1,
                    scannedAt = SCANNED_AT,
                    codes = listOf(
                        storedCode(id = 1),
                        storedCode(id = 2),
                    ),
                ),
            ),
        )
        val deleteScannedCodeUseCase = DeleteScannedCodeUseCaseImpl(scanRepository = scanRepository)

        // When
        deleteScannedCodeUseCase(codeId = 1)

        // Then
        assertEquals(
            expected = listOf(2L),
            actual = scanRepository.storedScans
                .single()
                .codes
                .map { it.id },
        )
    }

    private fun storedCode(id: Long): StoredCode =
        StoredCode(
            id = id,
            scanId = 1,
            rawValue = "https://scanny.app/$id",
            format = "QR_CODE",
            scannedAt = SCANNED_AT,
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(0)
    }
}
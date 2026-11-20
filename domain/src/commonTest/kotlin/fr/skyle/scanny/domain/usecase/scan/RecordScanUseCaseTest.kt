package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.fake.FixedClock
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RecordScanUseCaseTest {

    private val scanRepository = FakeScanRepository()

    private val recordScanUseCase = RecordScanUseCaseImpl(
        scanRepository = scanRepository,
        clock = FixedClock(instant = SCANNED_AT),
    )

    @Test
    fun `a scan is saved with its raw value its format and the current time`() = runTest {
        // Given
        val rawValue = "https://scanny.app"

        // When
        recordScanUseCase(
            rawValue = rawValue,
            format = BarcodeFormat.QR_CODE,
        )

        // Then
        assertEquals(
            expected = listOf(
                StoredScan(
                    id = 1,
                    rawValue = rawValue,
                    format = "QR_CODE",
                    scannedAt = SCANNED_AT,
                ),
            ),
            actual = scanRepository.storedScans,
        )
    }

    @Test
    fun `the recorded scan is returned with its id and its parsed content`() = runTest {
        // Given
        val rawValue = "https://scanny.app"

        // When
        val scan = recordScanUseCase(
            rawValue = rawValue,
            format = BarcodeFormat.QR_CODE,
        )

        // Then
        assertEquals(
            expected = Scan(
                id = 1,
                rawValue = rawValue,
                content = BarcodeContent.Url(url = rawValue),
                format = BarcodeFormat.QR_CODE,
                scannedAt = SCANNED_AT,
            ),
            actual = scan,
        )
    }

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
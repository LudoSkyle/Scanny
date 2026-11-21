package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.fake.FixedClock
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RecordScanUseCaseTest {

    private val scanRepository = FakeScanRepository()

    private val recordScanUseCase = recordScanUseCase(scanRepository = scanRepository)

    @Test
    fun `a scan is saved with its codes in their order and the current time`() = runTest {
        // When
        recordScanUseCase(
            barcodes = listOf(
                barcode(rawValue = "https://scanny.app"),
                barcode(
                    rawValue = "4006381333931",
                    format = BarcodeFormat.EAN_13,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                StoredScan(
                    id = 1,
                    scannedAt = SCANNED_AT,
                    codes = listOf(
                        storedCode(
                            id = 1,
                            scanId = 1,
                            rawValue = "https://scanny.app",
                            format = "QR_CODE",
                        ),
                        storedCode(
                            id = 2,
                            scanId = 1,
                            rawValue = "4006381333931",
                            format = "EAN_13",
                        ),
                    ),
                ),
            ),
            actual = scanRepository.storedScans,
        )
    }

    @Test
    fun `the recorded scan is returned with the ids and the parsed content of its codes`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                StoredScan(
                    id = 1,
                    scannedAt = SCANNED_AT,
                    codes = listOf(
                        storedCode(
                            id = 1,
                            scanId = 1,
                            rawValue = "Already there",
                        ),
                    ),
                ),
            ),
        )
        val recordScanUseCase = recordScanUseCase(scanRepository = scanRepository)

        // When
        val scan = recordScanUseCase(barcodes = listOf(barcode(rawValue = "https://scanny.app")))

        // Then
        assertEquals(
            expected = Scan(
                id = 2,
                scannedAt = SCANNED_AT,
                codes = listOf(
                    ScannedCode(
                        id = 2,
                        scanId = 2,
                        rawValue = "https://scanny.app",
                        content = BarcodeContent.Url(url = "https://scanny.app"),
                        format = BarcodeFormat.QR_CODE,
                        scannedAt = SCANNED_AT,
                    ),
                ),
            ),
            actual = scan,
        )
    }

    @Test
    fun `a upc a read as an ean 13 is recorded as a upc a`() = runTest {
        // When
        recordScanUseCase(
            barcodes = listOf(
                barcode(
                    rawValue = "0123456789012",
                    format = BarcodeFormat.EAN_13,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf("123456789012" to "UPC_A"),
            actual = recordedCodes(),
        )
    }

    @Test
    fun `a code read several times is recorded once at its first reading`() = runTest {
        // When
        recordScanUseCase(
            barcodes = listOf(
                barcode(rawValue = "First"),
                barcode(rawValue = "Second"),
                barcode(rawValue = "First"),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                "First" to "QR_CODE",
                "Second" to "QR_CODE",
            ),
            actual = recordedCodes(),
        )
    }

    @Test
    fun `a upc a read once as such and once as an ean 13 is recorded once`() = runTest {
        // When
        recordScanUseCase(
            barcodes = listOf(
                barcode(
                    rawValue = "123456789012",
                    format = BarcodeFormat.UPC_A,
                ),
                barcode(
                    rawValue = "0123456789012",
                    format = BarcodeFormat.EAN_13,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf("123456789012" to "UPC_A"),
            actual = recordedCodes(),
        )
    }

    @Test
    fun `the same value in two formats is recorded twice`() = runTest {
        // When
        recordScanUseCase(
            barcodes = listOf(
                barcode(rawValue = "Hello"),
                barcode(
                    rawValue = "Hello",
                    format = BarcodeFormat.DATA_MATRIX,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                "Hello" to "QR_CODE",
                "Hello" to "DATA_MATRIX",
            ),
            actual = recordedCodes(),
        )
    }

    @Test
    fun `a scan without code is refused and nothing is saved`() = runTest {
        // When
        assertFailsWith<IllegalArgumentException> {
            recordScanUseCase(barcodes = emptyList())
        }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = scanRepository.storedScans,
        )
    }

    @Test
    fun `a scan is recorded out of the history when the user keeps none`() = runTest {
        // Given
        val recordScanUseCase = recordScanUseCase(
            scanRepository = scanRepository,
            settingsRepository = FakeSettingsRepository(isScanHistoryEnabled = false),
        )

        // When
        val scan = recordScanUseCase(barcodes = listOf(barcode(rawValue = "Hello")))

        // Then
        assertEquals(
            expected = setOf(scan.id),
            actual = scanRepository.outOfHistoryIds,
        )
        assertEquals(
            expected = "Hello",
            actual = scan.codes.single().rawValue,
        )
    }

    @Test
    fun `the same codes as the latest capture a moment ago give it the new time`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 1,
                    rawValues = listOf("First", "Second"),
                    scannedAt = SCANNED_AT - 4.minutes,
                ),
            ),
        )
        val recordScanUseCase = recordScanUseCase(scanRepository = scanRepository)

        // When
        val scan = recordScanUseCase(
            barcodes = listOf(
                barcode(rawValue = "Second"),
                barcode(rawValue = "First"),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(1L to SCANNED_AT),
            actual = scanRepository.storedScans.map { storedScan -> storedScan.id to storedScan.scannedAt },
        )
        assertEquals(
            expected = 1,
            actual = scan.id,
        )
    }

    @Test
    fun `the same codes as the latest capture long ago are a new capture`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 1,
                    rawValues = listOf("Hello"),
                    scannedAt = SCANNED_AT - 6.minutes,
                ),
            ),
        )
        val recordScanUseCase = recordScanUseCase(scanRepository = scanRepository)

        // When
        recordScanUseCase(barcodes = listOf(barcode(rawValue = "Hello")))

        // Then
        assertEquals(
            expected = 2,
            actual = scanRepository.storedScans.size,
        )
    }

    @Test
    fun `other codes than the latest capture are a new capture`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 1,
                    rawValues = listOf("Hello"),
                    scannedAt = SCANNED_AT,
                ),
            ),
        )
        val recordScanUseCase = recordScanUseCase(scanRepository = scanRepository)

        // When
        recordScanUseCase(barcodes = listOf(barcode(rawValue = "World")))

        // Then
        assertEquals(
            expected = 2,
            actual = scanRepository.storedScans.size,
        )
    }

    @Test
    fun `the same codes are a new capture when the user keeps the duplicates`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 1,
                    rawValues = listOf("Hello"),
                    scannedAt = SCANNED_AT,
                ),
            ),
        )
        val recordScanUseCase = recordScanUseCase(
            scanRepository = scanRepository,
            settingsRepository = FakeSettingsRepository(isConsecutiveDuplicateSkipped = false),
        )

        // When
        recordScanUseCase(barcodes = listOf(barcode(rawValue = "Hello")))

        // Then
        assertEquals(
            expected = 2,
            actual = scanRepository.storedScans.size,
        )
    }

    private fun recordScanUseCase(
        scanRepository: FakeScanRepository,
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
    ): RecordScanUseCaseImpl =
        RecordScanUseCaseImpl(
            scanRepository = scanRepository,
            settingsRepository = settingsRepository,
            clock = FixedClock(instant = SCANNED_AT),
        )

    private fun storedScan(
        id: Long,
        rawValues: List<String>,
        scannedAt: Instant,
    ): StoredScan =
        StoredScan(
            id = id,
            scannedAt = scannedAt,
            codes = rawValues.mapIndexed { index, rawValue ->
                storedCode(
                    id = id * 10 + index,
                    scanId = id,
                    rawValue = rawValue,
                ).copy(scannedAt = scannedAt)
            },
        )

    private fun recordedCodes(): List<Pair<String, String>> =
        scanRepository.storedScans
            .single()
            .codes
            .map { code ->
                code.rawValue to code.format
            }

    private fun barcode(
        rawValue: String,
        format: BarcodeFormat = BarcodeFormat.QR_CODE,
    ): DetectedBarcode =
        DetectedBarcode(
            rawValue = rawValue,
            format = format,
        )

    private fun storedCode(
        id: Long,
        scanId: Long,
        rawValue: String,
        format: String = "QR_CODE",
    ): StoredCode =
        StoredCode(
            id = id,
            scanId = scanId,
            rawValue = rawValue,
            format = format,
            scannedAt = SCANNED_AT,
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
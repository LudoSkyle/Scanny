package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class WatchScansUseCaseTest {

    @Test
    fun `the stored scans are watched in their order with the parsed content of their codes`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 2,
                    codes = listOf(
                        storedCode(
                            id = 2,
                            scanId = 2,
                            rawValue = "WIFI:T:WPA;S:Home;P:secret;;",
                        ),
                        storedCode(
                            id = 3,
                            scanId = 2,
                            rawValue = "Hello",
                        ),
                    ),
                ),
                storedScan(
                    id = 1,
                    codes = listOf(
                        storedCode(
                            id = 1,
                            scanId = 1,
                            rawValue = "https://scanny.app",
                        ),
                    ),
                ),
            ),
        )
        val watchScansUseCase = WatchScansUseCaseImpl(scanRepository = scanRepository)

        // When
        val scans = watchScansUseCase().first()

        // Then
        assertEquals(
            expected = listOf(
                listOf(
                    BarcodeContent.Wifi(
                        ssid = "Home",
                        encryption = WifiEncryption.WPA,
                        password = "secret",
                    ),
                    BarcodeContent.Text(text = "Hello"),
                ),
                listOf(BarcodeContent.Url(url = "https://scanny.app")),
            ),
            actual = scans.map { scan ->
                scan.codes.map { it.content }
            },
        )
    }

    @Test
    fun `a stored format unknown to this version gives no format`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 1,
                    codes = listOf(
                        storedCode(
                            id = 1,
                            scanId = 1,
                            rawValue = "Hello",
                            format = "FUTURE_FORMAT",
                        ),
                    ),
                ),
            ),
        )
        val watchScansUseCase = WatchScansUseCaseImpl(scanRepository = scanRepository)

        // When
        val scans = watchScansUseCase().first()

        // Then
        assertEquals(
            expected = listOf(
                Scan(
                    id = 1,
                    scannedAt = SCANNED_AT,
                    codes = listOf(
                        ScannedCode(
                            id = 1,
                            scanId = 1,
                            rawValue = "Hello",
                            content = BarcodeContent.Text(text = "Hello"),
                            format = null,
                            scannedAt = SCANNED_AT,
                            isFavorite = false,
                            isInGroup = false,
                        ),
                    ),
                    isFavorite = false,
                ),
            ),
            actual = scans,
        )
    }

    private fun storedScan(
        id: Long,
        codes: List<StoredCode>,
    ): StoredScan =
        StoredScan(
            id = id,
            scannedAt = SCANNED_AT,
            codes = codes,
            isFavorite = false,
        )

    private fun storedCode(
        id: Long,
        scanId: Long,
        rawValue: String,
        format: String = BarcodeFormat.QR_CODE.name,
    ): StoredCode =
        StoredCode(
            id = id,
            scanId = scanId,
            rawValue = rawValue,
            format = format,
            scannedAt = SCANNED_AT,
            isScanFavorite = false,
            scanCodeCount = 1,
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
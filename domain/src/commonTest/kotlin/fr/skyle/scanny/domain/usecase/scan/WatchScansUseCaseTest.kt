package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class WatchScansUseCaseTest {

    @Test
    fun `the stored scans are watched in their order with their parsed content`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 2,
                    rawValue = "WIFI:T:WPA;S:Home;P:secret;;",
                ),
                storedScan(
                    id = 1,
                    rawValue = "Hello",
                ),
            ),
        )
        val watchScansUseCase = WatchScansUseCaseImpl(scanRepository = scanRepository)

        // When
        val scans = watchScansUseCase().first()

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Wifi(
                    ssid = "Home",
                    encryption = WifiEncryption.WPA,
                    password = "secret",
                ),
                BarcodeContent.Text(text = "Hello"),
            ),
            actual = scans.map { it.content },
        )
    }

    @Test
    fun `a stored format unknown to this version gives no format`() = runTest {
        // Given
        val scanRepository = FakeScanRepository(
            storedScans = listOf(
                storedScan(
                    id = 1,
                    rawValue = "Hello",
                    format = "FUTURE_FORMAT",
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
                    rawValue = "Hello",
                    content = BarcodeContent.Text(text = "Hello"),
                    format = null,
                    scannedAt = SCANNED_AT,
                ),
            ),
            actual = scans,
        )
    }

    private fun storedScan(
        id: Long,
        rawValue: String,
        format: String = BarcodeFormat.QR_CODE.name,
    ): StoredScan =
        StoredScan(
            id = id,
            rawValue = rawValue,
            format = format,
            scannedAt = SCANNED_AT,
        )

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
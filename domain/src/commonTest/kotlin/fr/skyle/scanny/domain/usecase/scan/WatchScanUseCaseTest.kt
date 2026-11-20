package fr.skyle.scanny.domain.usecase.scan

import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.domain.fake.FakeScanRepository
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Scan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class WatchScanUseCaseTest {

    private val scanRepository = FakeScanRepository(
        storedScans = listOf(
            StoredScan(
                id = 1,
                rawValue = "SMSTO:+33600000000:Hello",
                format = BarcodeFormat.QR_CODE.name,
                scannedAt = SCANNED_AT,
            ),
        ),
    )

    private val watchScanUseCase = WatchScanUseCaseImpl(scanRepository = scanRepository)

    @Test
    fun `a stored scan is watched with its parsed content`() = runTest {
        // When
        val scan = watchScanUseCase(scanId = 1).first()

        // Then
        assertEquals(
            expected = Scan(
                id = 1,
                rawValue = "SMSTO:+33600000000:Hello",
                content = BarcodeContent.Sms(
                    phoneNumber = "+33600000000",
                    message = "Hello",
                ),
                format = BarcodeFormat.QR_CODE,
                scannedAt = SCANNED_AT,
            ),
            actual = scan,
        )
    }

    @Test
    fun `a scan that does not exist is watched as null`() = runTest {
        // When
        val scan = watchScanUseCase(scanId = 404).first()

        // Then
        assertNull(scan)
    }

    private companion object {
        val SCANNED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
package fr.skyle.scanny.data.repository.scan

import fr.skyle.scanny.data.db.DatabaseTest
import fr.skyle.scanny.data.model.StoredScan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

internal class ScanRepositoryImplTest : DatabaseTest() {

    private val scanRepository = ScanRepositoryImpl(scanDao = scanDao)

    @Test
    fun `a saved scan is read back with its time to the millisecond`() = runTest {
        // Given
        val scannedAt = Instant.fromEpochMilliseconds(1_700_000_000_123)

        // When
        val id = scanRepository.saveScan(
            rawValue = "https://scanny.app",
            format = "QR_CODE",
            scannedAt = scannedAt,
        )

        // Then
        assertEquals(
            expected = StoredScan(
                id = id,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                scannedAt = scannedAt,
            ),
            actual = scanRepository.watchScan(id = id).first(),
        )
    }

    @Test
    fun `the scans are watched from the newest to the oldest`() = runTest {
        // Given
        val oldestId = scanRepository.saveScan(
            rawValue = "oldest",
            format = "EAN_13",
            scannedAt = Instant.fromEpochMilliseconds(1_000),
        )
        val newestId = scanRepository.saveScan(
            rawValue = "newest",
            format = "QR_CODE",
            scannedAt = Instant.fromEpochMilliseconds(2_000),
        )

        // When
        val scans = scanRepository.watchScans().first()

        // Then
        assertEquals(
            expected = listOf(
                StoredScan(
                    id = newestId,
                    rawValue = "newest",
                    format = "QR_CODE",
                    scannedAt = Instant.fromEpochMilliseconds(2_000),
                ),
                StoredScan(
                    id = oldestId,
                    rawValue = "oldest",
                    format = "EAN_13",
                    scannedAt = Instant.fromEpochMilliseconds(1_000),
                ),
            ),
            actual = scans,
        )
    }

    @Test
    fun `a deleted scan is no longer watched`() = runTest {
        // Given
        val id = scanRepository.saveScan(
            rawValue = "https://scanny.app",
            format = "QR_CODE",
            scannedAt = Instant.fromEpochMilliseconds(1_000),
        )

        // When
        scanRepository.deleteScan(id = id)

        // Then
        assertNull(scanRepository.watchScan(id = id).first())
    }
}
package fr.skyle.scanny.data.repository.scan

import fr.skyle.scanny.data.db.DatabaseTest
import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

internal class ScanRepositoryImplTest : DatabaseTest() {

    private val scanRepository = ScanRepositoryImpl(scanDao = scanDao)

    @Test
    fun `a saved capture is read back with its codes in their order and its time to the millisecond`() = runTest {
        // Given
        val scannedAt = Instant.fromEpochMilliseconds(1_700_000_000_123)

        // When
        val scanId = scanRepository.saveScan(
            codes = listOf(
                CodeToSave(
                    rawValue = "first",
                    format = "QR_CODE",
                ),
                CodeToSave(
                    rawValue = "second",
                    format = "EAN_13",
                ),
                CodeToSave(
                    rawValue = "third",
                    format = "CODE_128",
                ),
            ),
            scannedAt = scannedAt,
            isInHistory = true,
        )

        // Then
        val scan = scanRepository.watchScan(id = scanId).first()
        assertEquals(
            expected = listOf(
                Triple("first", "QR_CODE", scannedAt),
                Triple("second", "EAN_13", scannedAt),
                Triple("third", "CODE_128", scannedAt),
            ),
            actual = scan?.codes?.map { code -> Triple(code.rawValue, code.format, code.scannedAt) },
        )
        assertEquals(
            expected = scannedAt,
            actual = scan?.scannedAt,
        )
    }

    @Test
    fun `a capture without code is refused`() = runTest {
        // When
        val failure = assertFailsWith<IllegalArgumentException> {
            scanRepository.saveScan(
                codes = emptyList(),
                scannedAt = Instant.fromEpochMilliseconds(0),
                isInHistory = true,
            )
        }

        // Then
        assertEquals(
            expected = "A scan holds at least one code",
            actual = failure.message,
        )
    }

    @Test
    fun `the captures are watched from the newest to the oldest`() = runTest {
        // Given
        val oldestId = saveScan(
            rawValue = "oldest",
            scannedAtMillis = 1_000,
        )
        val newestId = saveScan(
            rawValue = "newest",
            scannedAtMillis = 2_000,
        )

        // When
        val scans = scanRepository.watchScans().first()

        // Then
        assertEquals(
            expected = listOf(
                newestId to "newest",
                oldestId to "oldest",
            ),
            actual = scans.map { scan -> scan.id to scan.codes.single().rawValue },
        )
    }

    @Test
    fun `a code is watched with its capture and its time`() = runTest {
        // Given
        val scanId = saveScan(
            rawValue = "https://scanny.app",
            scannedAtMillis = 1_000,
        )
        val codeId = scanRepository.watchScan(id = scanId).first()!!.codes.single().id

        // When
        val code = scanRepository.watchCode(id = codeId).first()

        // Then
        assertEquals(
            expected = StoredCode(
                id = codeId,
                scanId = scanId,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                scannedAt = Instant.fromEpochMilliseconds(1_000),
                isScanFavorite = false,
                scanCodeCount = 1,
            ),
            actual = code,
        )
    }

    @Test
    fun `a capture put among the favorites is watched as such with its codes`() = runTest {
        // Given
        val scanId = saveScan(
            rawValue = "https://scanny.app",
            scannedAtMillis = 1_000,
        )

        // When
        scanRepository.setScanFavorite(
            id = scanId,
            isFavorite = true,
        )

        // Then
        val scan = scanRepository.watchScan(id = scanId).first()
        assertEquals(
            expected = true to true,
            actual = scan?.isFavorite to scan?.codes?.single()?.isScanFavorite,
        )
    }

    @Test
    fun `a deleted capture is no longer watched`() = runTest {
        // Given
        val scanId = saveScan()

        // When
        scanRepository.deleteScan(id = scanId)

        // Then
        assertNull(scanRepository.watchScan(id = scanId).first())
    }

    @Test
    fun `deleting all the captures leaves none`() = runTest {
        // Given
        saveScan(scannedAtMillis = 1_000)
        saveScan(scannedAtMillis = 2_000)

        // When
        scanRepository.deleteAllScans()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = scanRepository.watchScans().first(),
        )
    }

    @Test
    fun `deleting the last code of a capture deletes the capture and keeps the others`() = runTest {
        // Given
        val keptId = saveScan(scannedAtMillis = 1_000)
        val emptiedId = saveScan(scannedAtMillis = 2_000)
        val codeId = scanRepository.watchScan(id = emptiedId).first()!!.codes.single().id

        // When
        scanRepository.deleteCode(id = codeId)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanRepository.watchScans().first().map(StoredScan::id),
        )
    }

    @Test
    fun `the latest capture of the history is found and can be given a new time`() = runTest {
        // Given
        val latestId = saveScan(
            rawValue = "latest",
            scannedAtMillis = 2_000,
        )
        saveScan(
            rawValue = "out of the history",
            scannedAtMillis = 3_000,
            isInHistory = false,
        )

        // When
        scanRepository.updateScanTime(
            id = latestId,
            scannedAt = Instant.fromEpochMilliseconds(4_000),
        )

        // Then
        assertEquals(
            expected = latestId to Instant.fromEpochMilliseconds(4_000),
            actual = scanRepository.findLatestScan()?.let { scan -> scan.id to scan.scannedAt },
        )
    }

    @Test
    fun `the purge deletes the captures out of the history and the older ones`() = runTest {
        // Given
        saveScan(
            rawValue = "old",
            scannedAtMillis = 1_000,
        )
        val keptId = saveScan(
            rawValue = "recent",
            scannedAtMillis = 3_000,
        )
        val outId = saveScan(
            rawValue = "out of the history",
            scannedAtMillis = 3_000,
            isInHistory = false,
        )

        // When
        scanRepository.purgeScans(scannedBefore = Instant.fromEpochMilliseconds(2_000))

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanRepository.watchScans().first().map(StoredScan::id),
        )
        assertNull(scanRepository.watchScan(id = outId).first())
    }

    @Test
    fun `a purge without time keeps the whole history`() = runTest {
        // Given
        val keptId = saveScan(scannedAtMillis = 1_000)
        saveScan(
            scannedAtMillis = 1_000,
            isInHistory = false,
        )

        // When
        scanRepository.purgeScans(scannedBefore = null)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanRepository.watchScans().first().map(StoredScan::id),
        )
    }

    private suspend fun saveScan(
        rawValue: String = "https://scanny.app",
        scannedAtMillis: Long = 0,
        isInHistory: Boolean = true,
    ): Long =
        scanRepository.saveScan(
            codes = listOf(
                CodeToSave(
                    rawValue = rawValue,
                    format = "QR_CODE",
                ),
            ),
            scannedAt = Instant.fromEpochMilliseconds(scannedAtMillis),
            isInHistory = isInHistory,
        )
}
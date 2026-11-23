package fr.skyle.scanny.data.db.dao

import androidx.room.useReaderConnection
import fr.skyle.scanny.data.db.DatabaseTest
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity
import fr.skyle.scanny.data.db.relation.ScannedCodeWithScanTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ScanDaoTest : DatabaseTest() {

    @Test
    fun `a capture is recorded with its codes`() = runTest {
        // When
        val scanId = scanDao.insertWithCodes(
            scan = ScanEntity(scannedAt = 1_700_000_000_123),
            codes = listOf(
                codeEntity(
                    position = 0,
                    rawValue = "first",
                ),
                codeEntity(
                    position = 1,
                    rawValue = "second",
                ),
            ),
        )

        // Then
        val scan = scanDao.watchById(id = scanId).first()
        assertEquals(
            expected = ScanEntity(
                id = scanId,
                scannedAt = 1_700_000_000_123,
            ),
            actual = scan?.scan,
        )
        assertEquals(
            expected = setOf(
                scanId to "first",
                scanId to "second",
            ),
            actual = scan?.codes?.map { code -> code.scanId to code.rawValue }?.toSet(),
        )
    }

    @Test
    fun `the captures are watched from the newest to the oldest`() = runTest {
        // Given
        val oldestId = insertScan(scannedAt = 1_000)
        val newestId = insertScan(scannedAt = 3_000)
        val middleId = insertScan(scannedAt = 2_000)

        // When
        val scans = scanDao.watchHistory().first()

        // Then
        assertEquals(
            expected = listOf(
                newestId,
                middleId,
                oldestId,
            ),
            actual = scans.map { it.scan.id },
        )
    }

    @Test
    fun `two captures of the same millisecond come from the last recorded to the first`() = runTest {
        // Given
        val firstId = insertScan(scannedAt = 1_000)
        val secondId = insertScan(scannedAt = 1_000)

        // When
        val scans = scanDao.watchHistory().first()

        // Then
        assertEquals(
            expected = listOf(
                secondId,
                firstId,
            ),
            actual = scans.map { it.scan.id },
        )
    }

    @Test
    fun `a code is watched with the time of its capture`() = runTest {
        // Given
        val scanId = insertScan(scannedAt = 1_700_000_000_123)
        val codeId = scanDao.watchById(id = scanId).first()!!.codes.single().id

        // When
        val code = scanDao.watchCodeById(id = codeId).first()

        // Then
        assertEquals(
            expected = ScannedCodeWithScanTime(
                code = ScannedCodeEntity(
                    id = codeId,
                    scanId = scanId,
                    position = 0,
                    rawValue = "https://scanny.app",
                    format = "QR_CODE",
                ),
                scannedAt = 1_700_000_000_123,
            ),
            actual = code,
        )
    }

    @Test
    fun `watching a capture or a code that does not exist gives null`() = runTest {
        // Given
        insertScan()

        // When
        val scan = scanDao.watchById(id = 404).first()
        val code = scanDao.watchCodeById(id = 404).first()

        // Then
        assertNull(scan)
        assertNull(code)
    }

    @Test
    fun `deleting a capture deletes its codes and keeps the other captures`() = runTest {
        // Given
        val keptId = insertScan(scannedAt = 1_000)
        val deletedId = insertScan(scannedAt = 2_000)
        val deletedCodeId = scanDao.watchById(id = deletedId).first()!!.codes.single().id

        // When
        scanDao.deleteById(id = deletedId)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanDao.watchHistory().first().map { it.scan.id },
        )
        assertNull(scanDao.watchCodeById(id = deletedCodeId).first())
        // Counted in the table itself: the code of a capture is read through a join with it
        assertEquals(
            expected = 1,
            actual = countCodes(),
        )
    }

    @Test
    fun `deleting everything deletes every capture and every code`() = runTest {
        // Given
        insertScan(scannedAt = 1_000)
        insertScan(scannedAt = 2_000)

        // When
        scanDao.deleteAll()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = scanDao.watchHistory().first(),
        )
        assertEquals(
            expected = 0,
            actual = countCodes(),
        )
    }

    @Test
    fun `deleting the last code of a capture deletes the capture and keeps the others`() = runTest {
        // Given
        val keptId = insertScan(scannedAt = 1_000)
        val emptiedId = insertScan(scannedAt = 2_000)
        val codeId = scanDao.watchById(id = emptiedId).first()!!.codes.single().id

        // When
        scanDao.deleteCodeAndEmptyScan(id = codeId)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanDao.watchHistory().first().map { it.scan.id },
        )
    }

    @Test
    fun `deleting a code of a capture that has others keeps the capture`() = runTest {
        // Given
        val scanId = scanDao.insertWithCodes(
            scan = ScanEntity(scannedAt = 1_000),
            codes = listOf(
                codeEntity(
                    position = 0,
                    rawValue = "deleted",
                ),
                codeEntity(
                    position = 1,
                    rawValue = "kept",
                ),
            ),
        )
        val deletedCodeId = scanDao.watchById(id = scanId).first()!!.codes.single { it.rawValue == "deleted" }.id

        // When
        scanDao.deleteCodeAndEmptyScan(id = deletedCodeId)

        // Then
        assertEquals(
            expected = listOf("kept"),
            actual = scanDao.watchById(id = scanId).first()?.codes?.map { it.rawValue },
        )
    }

    @Test
    fun `a capture out of the history is watched by its id only`() = runTest {
        // Given
        val keptId = insertScan()
        val outId = insertScan(isInHistory = false)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanDao.watchHistory().first().map { it.scan.id },
        )
        assertEquals(
            expected = outId,
            actual = scanDao.watchById(id = outId).first()?.scan?.id,
        )
    }

    @Test
    fun `the latest capture of the history leaves out the captures out of it`() = runTest {
        // Given
        val latestId = insertScan(scannedAt = 2_000)
        insertScan(scannedAt = 1_000)
        insertScan(
            scannedAt = 3_000,
            isInHistory = false,
        )

        // When
        val latest = scanDao.findLatestInHistory()

        // Then
        assertEquals(
            expected = latestId,
            actual = latest?.scan?.id,
        )
    }

    @Test
    fun `a capture given a new time moves in the history`() = runTest {
        // Given
        val movedId = insertScan(scannedAt = 1_000)
        val otherId = insertScan(scannedAt = 2_000)

        // When
        scanDao.updateScannedAt(
            id = movedId,
            scannedAt = 3_000,
        )

        // Then
        assertEquals(
            expected = listOf(movedId, otherId),
            actual = scanDao.watchHistory().first().map { it.scan.id },
        )
    }

    @Test
    fun `the captures out of the history are deleted with their codes`() = runTest {
        // Given
        val keptId = insertScan()
        insertScan(isInHistory = false)

        // When
        scanDao.deleteOutOfHistory()

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanDao.watchHistory().first().map { it.scan.id },
        )
        assertEquals(
            expected = 1,
            actual = countCodes(),
        )
    }

    @Test
    fun `the captures scanned before a time are deleted and the later ones kept`() = runTest {
        // Given
        insertScan(scannedAt = 1_000)
        val keptId = insertScan(scannedAt = 2_000)

        // When
        scanDao.deleteScannedBefore(before = 2_000)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanDao.watchHistory().first().map { it.scan.id },
        )
    }

    private suspend fun countCodes(): Long =
        database.useReaderConnection { connection ->
            connection.usePrepared("SELECT COUNT(*) FROM scanned_code") { statement ->
                statement.step()
                statement.getLong(0)
            }
        }

    private suspend fun insertScan(
        scannedAt: Long = 0,
        isInHistory: Boolean = true,
    ): Long =
        scanDao.insertWithCodes(
            scan = ScanEntity(
                scannedAt = scannedAt,
                isInHistory = isInHistory,
            ),
            codes = listOf(codeEntity()),
        )

    private fun codeEntity(
        position: Int = 0,
        rawValue: String = "https://scanny.app",
        format: String = "QR_CODE",
    ): ScannedCodeEntity =
        ScannedCodeEntity(
            scanId = 0,
            position = position,
            rawValue = rawValue,
            format = format,
        )
}
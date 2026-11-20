package fr.skyle.scanny.data.db.dao

import fr.skyle.scanny.data.db.DatabaseTest
import fr.skyle.scanny.data.db.entity.ScanEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ScanDaoTest : DatabaseTest() {

    @Test
    fun `a recorded scan keeps all its fields`() = runTest {
        // Given
        val id = scanDao.insert(
            entity = scanEntity(
                rawValue = "WIFI:S:Home;;",
                format = "QR_CODE",
                scannedAt = 1_700_000_000_123,
            ),
        )

        // When
        val scan = scanDao.watchById(id = id).first()

        // Then
        assertEquals(
            expected = ScanEntity(
                id = id,
                rawValue = "WIFI:S:Home;;",
                format = "QR_CODE",
                scannedAt = 1_700_000_000_123,
            ),
            actual = scan,
        )
    }

    @Test
    fun `the scans are watched from the newest to the oldest`() = runTest {
        // Given
        val oldestId = scanDao.insert(entity = scanEntity(scannedAt = 1_000))
        val newestId = scanDao.insert(entity = scanEntity(scannedAt = 3_000))
        val middleId = scanDao.insert(entity = scanEntity(scannedAt = 2_000))

        // When
        val scans = scanDao.watchAll().first()

        // Then
        assertEquals(
            expected = listOf(
                newestId,
                middleId,
                oldestId,
            ),
            actual = scans.map { it.id },
        )
    }

    @Test
    fun `two scans of the same millisecond come from the last recorded to the first`() = runTest {
        // Given
        val firstId = scanDao.insert(entity = scanEntity(scannedAt = 1_000))
        val secondId = scanDao.insert(entity = scanEntity(scannedAt = 1_000))

        // When
        val scans = scanDao.watchAll().first()

        // Then
        assertEquals(
            expected = listOf(
                secondId,
                firstId,
            ),
            actual = scans.map { it.id },
        )
    }

    @Test
    fun `watching a scan that does not exist gives null`() = runTest {
        // Given
        scanDao.insert(entity = scanEntity())

        // When
        val scan = scanDao.watchById(id = 404).first()

        // Then
        assertNull(scan)
    }

    @Test
    fun `deleting a scan keeps the others`() = runTest {
        // Given
        val keptId = scanDao.insert(entity = scanEntity(scannedAt = 1_000))
        val deletedId = scanDao.insert(entity = scanEntity(scannedAt = 2_000))

        // When
        scanDao.deleteById(id = deletedId)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = scanDao.watchAll().first().map { it.id },
        )
    }

    private fun scanEntity(
        rawValue: String = "https://scanny.app",
        format: String = "QR_CODE",
        scannedAt: Long = 0,
    ): ScanEntity =
        ScanEntity(
            rawValue = rawValue,
            format = format,
            scannedAt = scannedAt,
        )
}
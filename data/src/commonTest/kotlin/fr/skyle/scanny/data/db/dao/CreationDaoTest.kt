package fr.skyle.scanny.data.db.dao

import fr.skyle.scanny.data.db.DatabaseTest
import fr.skyle.scanny.data.db.entity.CreationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CreationDaoTest : DatabaseTest() {

    @Test
    fun `a creation is recorded with how it is drawn and its logo and watched by its id`() = runTest {
        // When
        val creationId = creationDao.insert(
            entity = CreationEntity(
                createdAt = 1_700_000_000_123,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                darkColor = 0xFF1A237E,
                lightColor = 0xFFFFF8E1,
                dotShape = "CIRCLE",
                cornerShape = "ROUNDED",
                logo = byteArrayOf(1, 2, 3),
            ),
        )

        // Then
        assertEquals(
            expected = CreationEntity(
                id = creationId,
                createdAt = 1_700_000_000_123,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                darkColor = 0xFF1A237E,
                lightColor = 0xFFFFF8E1,
                dotShape = "CIRCLE",
                cornerShape = "ROUNDED",
                logo = byteArrayOf(1, 2, 3),
            ),
            actual = creationDao.watchById(id = creationId).first(),
        )
    }

    @Test
    fun `the history of the creations goes from the newest to the oldest and leaves out the others`() = runTest {
        // Given
        val oldestId = insertCreation(createdAt = 1_000)
        val newestId = insertCreation(createdAt = 2_000)
        insertCreation(
            createdAt = 3_000,
            isInHistory = false,
        )

        // When
        val creations = creationDao.watchHistory().first()

        // Then
        assertEquals(
            expected = listOf(newestId, oldestId),
            actual = creations.map { creation -> creation.id },
        )
    }

    @Test
    fun `a deleted creation is no longer watched`() = runTest {
        // Given
        val creationId = insertCreation()

        // When
        creationDao.deleteById(id = creationId)

        // Then
        assertNull(creationDao.watchById(id = creationId).first())
    }

    @Test
    fun `deleting everything leaves no creation`() = runTest {
        // Given
        insertCreation()
        insertCreation(isInHistory = false)

        // When
        creationDao.deleteAll()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = creationDao.watchHistory().first(),
        )
    }

    @Test
    fun `the creations out of the history and the older ones are deleted`() = runTest {
        // Given
        insertCreation(createdAt = 1_000)
        val keptId = insertCreation(createdAt = 2_000)
        val outId = insertCreation(
            createdAt = 2_000,
            isInHistory = false,
        )

        // When
        creationDao.deleteOutOfHistory()
        creationDao.deleteCreatedBefore(before = 2_000)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = creationDao.watchHistory().first().map { creation -> creation.id },
        )
        assertNull(creationDao.watchById(id = outId).first())
    }

    private suspend fun insertCreation(
        createdAt: Long = 0,
        isInHistory: Boolean = true,
    ): Long =
        creationDao.insert(
            entity = CreationEntity(
                createdAt = createdAt,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                darkColor = 0xFF000000,
                lightColor = 0xFFFFFFFF,
                dotShape = "SQUARE",
                cornerShape = "SQUARE",
                logo = null,
                isInHistory = isInHistory,
            ),
        )
}
package fr.skyle.scanny.data.repository.creation

import fr.skyle.scanny.data.db.DatabaseTest
import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.data.model.StoredCreation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

internal class CreationRepositoryImplTest : DatabaseTest() {

    private val creationRepository = CreationRepositoryImpl(creationDao = creationDao)

    @Test
    fun `a saved creation is read back with its style and its time to the millisecond`() = runTest {
        // Given
        val createdAt = Instant.fromEpochMilliseconds(1_700_000_000_123)
        val style = StoredCodeStyle(
            darkColor = 0xFF1A237E,
            lightColor = 0xFFFFF8E1,
            dotShape = "CIRCLE",
            cornerShape = "ROUNDED",
        )

        // When
        val creationId = creationRepository.saveCreation(
            rawValue = "https://scanny.app",
            format = "QR_CODE",
            style = style,
            createdAt = createdAt,
            isInHistory = true,
        )

        // Then
        assertEquals(
            expected = StoredCreation(
                id = creationId,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                style = style,
                createdAt = createdAt,
            ),
            actual = creationRepository.watchCreation(id = creationId).first(),
        )
    }

    @Test
    fun `a creation out of the history is not watched with it`() = runTest {
        // Given
        val keptId = saveCreation(createdAtMillis = 1_000)
        saveCreation(
            createdAtMillis = 2_000,
            isInHistory = false,
        )

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = creationRepository.watchCreations().first().map(StoredCreation::id),
        )
    }

    @Test
    fun `the purge deletes the creations out of the history and the older ones`() = runTest {
        // Given
        saveCreation(createdAtMillis = 1_000)
        val keptId = saveCreation(createdAtMillis = 3_000)
        val outId = saveCreation(
            createdAtMillis = 3_000,
            isInHistory = false,
        )

        // When
        creationRepository.purgeCreations(createdBefore = Instant.fromEpochMilliseconds(2_000))

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = creationRepository.watchCreations().first().map(StoredCreation::id),
        )
        assertNull(creationRepository.watchCreation(id = outId).first())
    }

    @Test
    fun `a purge without time keeps the whole history of the creations`() = runTest {
        // Given
        val keptId = saveCreation(createdAtMillis = 1_000)

        // When
        creationRepository.purgeCreations(createdBefore = null)

        // Then
        assertEquals(
            expected = listOf(keptId),
            actual = creationRepository.watchCreations().first().map(StoredCreation::id),
        )
    }

    @Test
    fun `deleted creations are no longer watched`() = runTest {
        // Given
        val deletedId = saveCreation()
        val otherId = saveCreation()

        // When
        creationRepository.deleteCreation(id = deletedId)
        val afterOne = creationRepository.watchCreations().first().map(StoredCreation::id)
        creationRepository.deleteAllCreations()

        // Then
        assertEquals(
            expected = listOf(otherId) to emptyList(),
            actual = afterOne to creationRepository.watchCreations().first().map(StoredCreation::id),
        )
    }

    private suspend fun saveCreation(
        createdAtMillis: Long = 0,
        isInHistory: Boolean = true,
    ): Long =
        creationRepository.saveCreation(
            rawValue = "https://scanny.app",
            format = "QR_CODE",
            style = StoredCodeStyle(
                darkColor = 0xFF000000,
                lightColor = 0xFFFFFFFF,
                dotShape = "SQUARE",
                cornerShape = "SQUARE",
            ),
            createdAt = Instant.fromEpochMilliseconds(createdAtMillis),
            isInHistory = isInHistory,
        )
}
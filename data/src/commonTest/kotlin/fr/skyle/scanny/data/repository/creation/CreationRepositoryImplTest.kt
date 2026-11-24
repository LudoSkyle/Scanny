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
    fun `a saved creation is read back with its style its logo and its time to the millisecond`() = runTest {
        // Given
        val createdAt = Instant.fromEpochMilliseconds(1_700_000_000_123)
        val style = StoredCodeStyle(
            darkColor = 0xFF1A237E,
            lightColor = 0xFFFFF8E1,
            cornerColor = 0xFFB71C1C,
            gradient = "RADIAL",
            gradientColor = 0xFF1B5E20,
            dotShape = "CIRCLE",
            cornerShape = "ROUNDED",
            logo = byteArrayOf(1, 2, 3),
            logoShape = "CIRCLE",
            logoMargin = "LARGE",
            errorCorrection = "HIGH",
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
                // The logo read back from the database, another array of the same bytes
                style = style.copy(logo = byteArrayOf(1, 2, 3)),
                createdAt = createdAt,
                isFavorite = false,
            ),
            actual = creationRepository.watchCreation(id = creationId).first(),
        )
    }

    @Test
    fun `a creation put among the favorites is watched as such`() = runTest {
        // Given
        val creationId = creationRepository.saveCreation(
            rawValue = "https://scanny.app",
            format = "QR_CODE",
            style = StoredCodeStyle(
                darkColor = 0xFF000000,
                lightColor = 0xFFFFFFFF,
                cornerColor = null,
                gradient = "NONE",
                gradientColor = 0xFF3B43BE,
                dotShape = "SQUARE",
                cornerShape = "SQUARE",
                logo = null,
                logoShape = "SQUARE",
                logoMargin = "NORMAL",
                errorCorrection = "AUTO",
            ),
            createdAt = Instant.fromEpochMilliseconds(1_000),
            isInHistory = true,
        )

        // When
        creationRepository.setCreationFavorite(
            id = creationId,
            isFavorite = true,
        )

        // Then
        assertEquals(
            expected = true,
            actual = creationRepository.watchCreation(id = creationId).first()?.isFavorite,
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
                cornerColor = null,
                gradient = "NONE",
                gradientColor = 0xFF3B43BE,
                dotShape = "SQUARE",
                cornerShape = "SQUARE",
                logo = null,
                logoShape = "SQUARE",
                logoMargin = "NORMAL",
                errorCorrection = "AUTO",
            ),
            createdAt = Instant.fromEpochMilliseconds(createdAtMillis),
            isInHistory = isInHistory,
        )
}
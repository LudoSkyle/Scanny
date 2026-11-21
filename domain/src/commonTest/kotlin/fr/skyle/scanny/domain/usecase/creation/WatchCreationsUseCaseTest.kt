package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.data.model.StoredCreation
import fr.skyle.scanny.domain.fake.FakeCreationRepository
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.Creation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class WatchCreationsUseCaseTest {

    private val creationRepository = FakeCreationRepository(
        storedCreations = listOf(
            StoredCreation(
                id = 1,
                rawValue = "https://scanny.app",
                format = "QR_CODE",
                style = StoredCodeStyle(
                    darkColor = 0xFF1A237E,
                    lightColor = 0xFFFFF8E1,
                    dotShape = "CIRCLE",
                    cornerShape = "ROUNDED",
                    logo = byteArrayOf(1, 2, 3),
                ),
                createdAt = CREATED_AT,
            ),
        ),
    )

    @Test
    fun `the creations are watched with their content read from their text their style and their logo`() = runTest {
        // When
        val creations = WatchCreationsUseCaseImpl(creationRepository = creationRepository)().first()

        // Then
        assertEquals(
            expected = listOf(
                Creation(
                    id = 1,
                    content = BarcodeContent.Url(url = "https://scanny.app"),
                    rawValue = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                    style = CodeStyle(
                        darkColor = 0xFF1A237E,
                        lightColor = 0xFFFFF8E1,
                        dotShape = CodeShape.CIRCLE,
                        cornerShape = CodeShape.ROUNDED,
                        logo = byteArrayOf(1, 2, 3),
                    ),
                    createdAt = CREATED_AT,
                ),
            ),
            actual = creations,
        )
    }

    @Test
    fun `a shape this version does not know is drawn square`() = runTest {
        // Given
        val creationRepository = FakeCreationRepository(
            storedCreations = listOf(
                StoredCreation(
                    id = 1,
                    rawValue = "https://scanny.app",
                    format = "QR_CODE",
                    style = StoredCodeStyle(
                        darkColor = 0xFF000000,
                        lightColor = 0xFFFFFFFF,
                        dotShape = "STAR",
                        cornerShape = "HEART",
                        logo = null,
                    ),
                    createdAt = CREATED_AT,
                ),
            ),
        )

        // When
        val creation = WatchCreationUseCaseImpl(creationRepository = creationRepository)(creationId = 1).first()

        // Then
        assertEquals(
            expected = CodeStyle(),
            actual = creation?.style,
        )
    }

    @Test
    fun `a creation is watched by its id and gives null once deleted`() = runTest {
        // Given
        val watchCreationUseCase = WatchCreationUseCaseImpl(creationRepository = creationRepository)
        val found = watchCreationUseCase(creationId = 1).first()

        // When
        DeleteCreationUseCaseImpl(creationRepository = creationRepository)(creationId = 1)

        // Then
        assertEquals(
            expected = 1L,
            actual = found?.id,
        )
        assertNull(watchCreationUseCase(creationId = 1).first())
    }

    @Test
    fun `deleting all the creations leaves none`() = runTest {
        // When
        DeleteAllCreationsUseCaseImpl(creationRepository = creationRepository)()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = WatchCreationsUseCaseImpl(creationRepository = creationRepository)().first(),
        )
    }

    private companion object {
        val CREATED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
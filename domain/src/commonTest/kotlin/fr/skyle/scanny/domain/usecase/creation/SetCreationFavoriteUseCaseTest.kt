package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.data.model.StoredCreation
import fr.skyle.scanny.domain.fake.FakeCreationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SetCreationFavoriteUseCaseTest {

    @Test
    fun `a creation is put among the favorites and taken out`() = runTest {
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
                    isFavorite = false,
                ),
            ),
        )
        val useCase = SetCreationFavoriteUseCaseImpl(creationRepository = creationRepository)

        // When
        useCase(creationId = 1, isFavorite = true)
        val favorite = creationRepository.watchCreation(id = 1).first()?.isFavorite
        useCase(creationId = 1, isFavorite = false)

        // Then
        assertEquals(
            expected = true to false,
            actual = favorite to creationRepository.watchCreation(id = 1).first()?.isFavorite,
        )
    }
}

package fr.skyle.scanny.domain.payload

import kotlin.test.Test
import kotlin.test.assertEquals

class CoordinatesTest {

    @Test
    fun `a coordinate is written in decimals rounded to seven places`() {
        // Given
        val coordinates = listOf(48.8584, -122.00000012345, 0.00001, 2.0, -0.00000001)

        // When
        val texts = coordinates.map { coordinate -> coordinate.toCoordinateText() }

        // Then
        assertEquals(
            expected = listOf("48.8584", "-122.0000001", "0.00001", "2", "0"),
            actual = texts,
        )
    }
}

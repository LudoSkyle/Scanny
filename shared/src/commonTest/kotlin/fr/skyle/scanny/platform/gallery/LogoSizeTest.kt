package fr.skyle.scanny.platform.gallery

import kotlin.test.Test
import kotlin.test.assertEquals

class LogoSizeTest {

    @Test
    fun `a large image is reduced to the longest side of a logo with its proportions`() {
        // When
        val size = logoSize(
            width = 4000,
            height = 3000,
        )

        // Then
        assertEquals(
            expected = 256 to 192,
            actual = size,
        )
    }

    @Test
    fun `a small image is not enlarged`() {
        // When
        val size = logoSize(
            width = 120,
            height = 80,
        )

        // Then
        assertEquals(
            expected = 120 to 80,
            actual = size,
        )
    }

    @Test
    fun `a thin image keeps a side of one pixel at least`() {
        // When
        val size = logoSize(
            width = 3000,
            height = 2,
        )

        // Then
        assertEquals(
            expected = 256 to 1,
            actual = size,
        )
    }
}

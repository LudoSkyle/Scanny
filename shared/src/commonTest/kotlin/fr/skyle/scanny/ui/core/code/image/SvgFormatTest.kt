package fr.skyle.scanny.ui.core.code.image

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class SvgFormatTest {

    @Test
    fun `a coordinate is written to the hundredth without useless zeros nor exponent`() {
        // When
        val texts = listOf(0f, 12f, 12.5f, 12.345f, -3.1f, 0.00001f, -0.001f).map(::format)

        // Then
        assertEquals(
            expected = listOf("0", "12", "12.5", "12.35", "-3.1", "0", "0"),
            actual = texts,
        )
    }

    @Test
    fun `a color is written as svg reads it and its opacity apart`() {
        // When
        val texts = listOf(Color(0xFF3B43BE), Color(0x80FFFFFF), Color.Black).map { color -> color.toSvgColor() }

        // Then
        assertEquals(
            expected = listOf("#3b43be", "#ffffff", "#000000"),
            actual = texts,
        )
    }
}

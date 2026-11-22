package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

class ColorMapperTest {

    @Test
    fun `the pure colors have their hue and the greys none`() {
        // When
        val colors = listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF000000, 0xFFFFFFFF).map { argb -> argb.toHsvColor() }

        // Then
        assertEquals(
            expected = listOf(
                HsvColor(hue = 0f, saturation = 1f, value = 1f),
                HsvColor(hue = 120f, saturation = 1f, value = 1f),
                HsvColor(hue = 240f, saturation = 1f, value = 1f),
                HsvColor(hue = 0f, saturation = 0f, value = 0f),
                HsvColor(hue = 0f, saturation = 0f, value = 1f),
            ),
            actual = colors,
        )
    }

    @Test
    fun `a color of the palette comes back from the picker as it was`() {
        // Given
        val palette = CodeDotColor.entries.map { color -> color.argb } + CodeBackgroundColor.entries.map { color -> color.argb }

        // When
        val shifted = palette.filterNot { argb -> argb.toHsvColor().toArgb().isCloseTo(argb) }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = shifted,
        )
    }

    @Test
    fun `six hexadecimal digits make an opaque color with or without their hash`() {
        // When
        val colors = listOf("3B43BE", "#1b5e20", " 000000 ").map { text -> text.hexToArgb() }

        // Then
        assertEquals(
            expected = listOf(0xFF3B43BE, 0xFF1B5E20, 0xFF000000),
            actual = colors,
        )
    }

    @Test
    fun `anything but six hexadecimal digits makes no color`() {
        // When
        val colors = listOf("", "3B43B", "3B43BE0", "GGGGGG", "#12").map { text -> text.hexToArgb() }

        // Then
        assertEquals(
            expected = listOf(null, null, null, null, null),
            actual = colors,
        )
    }

    @Test
    fun `a color is written as six hexadecimal digits in capitals`() {
        // When
        val codes = listOf(0xFF3B43BE, 0xFF00000A).map { argb -> argb.toHex() }

        // Then
        assertEquals(
            expected = listOf("3B43BE", "00000A"),
            actual = codes,
        )
    }

    @Test
    fun `red green and blue from 0 to 255 make an opaque color and its components come back`() {
        // When
        val argb = rgbToArgb(red = 59, green = 67, blue = 190)

        // Then
        assertEquals(
            expected = 0xFF3B43BE,
            actual = argb,
        )
        assertEquals(
            expected = Triple(59, 67, 190),
            actual = Triple(0xFF3B43BE.red, 0xFF3B43BE.green, 0xFF3B43BE.blue),
        )
    }

    @Test
    fun `a component out of 0 to 255 makes no color`() {
        // When
        val colors = listOf(
            rgbToArgb(red = 256, green = 0, blue = 0),
            rgbToArgb(red = 0, green = -1, blue = 0),
        )

        // Then
        assertEquals(
            expected = listOf(null, null),
            actual = colors,
        )
    }

    // The floats of the picker round each channel: one step off at most
    private fun Long.isCloseTo(other: Long): Boolean =
        listOf(24, 16, 8, 0).all { shift -> abs(((this shr shift) and 0xFF) - ((other shr shift) and 0xFF)) <= 1 }
}

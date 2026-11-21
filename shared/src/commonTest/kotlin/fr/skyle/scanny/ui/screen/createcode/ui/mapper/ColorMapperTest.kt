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

    // The floats of the picker round each channel: one step off at most
    private fun Long.isCloseTo(other: Long): Boolean =
        listOf(24, 16, 8, 0).all { shift -> abs(((this shr shift) and 0xFF) - ((other shr shift) and 0xFF)) <= 1 }
}

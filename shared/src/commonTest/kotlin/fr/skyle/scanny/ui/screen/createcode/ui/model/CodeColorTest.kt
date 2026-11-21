package fr.skyle.scanny.ui.screen.createcode.ui.model

import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeColorTest {

    @Test
    fun `every dot color stands out on every background color`() {
        // When
        val poorPairs = CodeDotColor.entries.flatMap { dotColor ->
            CodeBackgroundColor.entries
                .filter { backgroundColor -> contrastRatio(dotColor.argb, backgroundColor.argb) < MIN_CONTRAST_RATIO }
                .map { backgroundColor -> dotColor to backgroundColor }
        }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = poorPairs,
        )
    }

    // The contrast ratio of the WCAG, from 1 for the same color to 21 for black on white
    private fun contrastRatio(
        darkArgb: Long,
        lightArgb: Long,
    ): Double =
        (relativeLuminance(lightArgb) + 0.05) / (relativeLuminance(darkArgb) + 0.05)

    private fun relativeLuminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val value = ((argb shr shift) and 0xFF) / 255.0
            return if (value <= 0.03928) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(shift = 16) + 0.7152 * channel(shift = 8) + 0.0722 * channel(shift = 0)
    }

    private companion object {
        // The contrast the WCAG asks of a text: a scanner reads a code as well
        const val MIN_CONTRAST_RATIO = 4.5
    }
}

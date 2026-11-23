package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.test.ComposeTest
import fr.skyle.scanny.ui.test.RED_PNG
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import kotlin.test.Test
import kotlin.test.assertEquals

// A logo read for real: on the JVM, through the native graphics of Robolectric
internal class CodeStyleLogoTest : ComposeTest() {

    @Test
    fun `the dots around a logo are cleared by its margin and left under it without one`() {
        // When
        val paddings = CodeLogoMargin.entries.associateWith { margin ->
            CodeStyle(
                logo = RED_PNG,
                logoMargin = margin,
            ).toQrOptions().logo.padding
        }

        // Then
        assertEquals(
            expected = mapOf(
                CodeLogoMargin.NONE to QrLogoPadding.Empty,
                CodeLogoMargin.NORMAL to QrLogoPadding.Accurate(size = 0.1f),
                CodeLogoMargin.LARGE to QrLogoPadding.Accurate(size = 0.25f),
            ),
            actual = paddings,
        )
    }
}

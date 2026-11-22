package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.choose
import fr.skyle.scanny.resources.color_component_blue
import fr.skyle.scanny.resources.color_component_green
import fr.skyle.scanny.resources.color_component_red
import fr.skyle.scanny.resources.color_hex
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
internal class CustomColorSheetTest : ComposeTest() {

    @Test
    fun `a color typed in hexadecimal is chosen exactly and its components follow`() = runComposeUiTest {
        // Given
        val chosen = mutableListOf<Long>()
        setContent {
            customColorSheet(onColorChosen = { argb -> chosen += argb })
        }

        // When
        field(Res.string.color_hex).performTextReplacement("#1b5e20")
        onNodeWithText(getString(Res.string.choose)).performScrollTo().performClick()

        // Then
        field(Res.string.color_component_red).assert(hasText("27"))
        field(Res.string.color_component_green).assert(hasText("94"))
        field(Res.string.color_component_blue).assert(hasText("32"))
        assertEquals(
            expected = listOf(0xFF1B5E20),
            actual = chosen,
        )
    }

    @Test
    fun `a color typed in red green and blue is chosen and its code follows`() = runComposeUiTest {
        // Given
        val chosen = mutableListOf<Long>()
        setContent {
            customColorSheet(onColorChosen = { argb -> chosen += argb })
        }

        // When
        field(Res.string.color_component_red).performTextReplacement("255")
        field(Res.string.color_component_green).performTextReplacement("0")
        field(Res.string.color_component_blue).performTextReplacement("128")
        onNodeWithText(getString(Res.string.choose)).performScrollTo().performClick()

        // Then
        field(Res.string.color_hex).assert(hasText("FF0080"))
        assertEquals(
            expected = listOf(0xFFFF0080),
            actual = chosen,
        )
    }

    @Test
    fun `an incomplete code leaves the color as it was`() = runComposeUiTest {
        // Given
        val chosen = mutableListOf<Long>()
        setContent {
            customColorSheet(onColorChosen = { argb -> chosen += argb })
        }

        // When
        field(Res.string.color_hex).performTextReplacement("12")
        onNodeWithText(getString(Res.string.choose)).performScrollTo().performClick()

        // Then
        assertEquals(
            expected = listOf(INITIAL),
            actual = chosen,
        )
    }

    @Test
    fun `a vertical drag on the color area changes the color rather than scrolling the sheet`() = runComposeUiTest {
        // Given
        val chosen = mutableListOf<Long>()
        setContent {
            customColorSheet(onColorChosen = { argb -> chosen += argb })
        }

        // When
        // The area of saturation and brightness, the first part of the picker that tells what it is
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))[0].performTouchInput {
            swipe(
                start = center,
                end = Offset(x = center.x, y = bottom - 1),
            )
        }
        onNodeWithText(getString(Res.string.choose)).performScrollTo().performClick()

        // Then
        assertNotEquals(
            illegal = INITIAL,
            actual = chosen.single(),
        )
    }

    // The text field, told from its label by what it can do
    private suspend fun ComposeUiTest.field(label: StringResource): SemanticsNodeInteraction =
        onNode(hasSetTextAction() and hasText(getString(label)))

    @Composable
    private fun customColorSheet(onColorChosen: (Long) -> Unit) {
        SCTheme {
            CustomColorSheet(
                initialArgb = INITIAL,
                onColorChosen = onColorChosen,
                onDismissRequest = {},
            )
        }
    }

    private companion object {
        const val INITIAL = 0xFF3B43BE
    }
}

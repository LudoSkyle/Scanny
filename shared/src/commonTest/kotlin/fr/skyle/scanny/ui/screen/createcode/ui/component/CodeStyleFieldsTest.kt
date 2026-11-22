package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.choose
import fr.skyle.scanny.resources.code_low_contrast
import fr.skyle.scanny.resources.color_black
import fr.skyle.scanny.resources.color_cream
import fr.skyle.scanny.resources.color_indigo
import fr.skyle.scanny.resources.other_color
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CodeStyleFieldsTest : ComposeTest() {

    @Test
    fun `a color of the palette touched becomes the color of the dots`() = runComposeUiTest {
        // Given
        val styles = mutableListOf<CodeStyle>()
        setContent {
            codeStyleFields(
                style = CodeStyle(),
                onStyleChanged = { style -> styles += style },
            )
        }

        // When
        onNodeWithContentDescription(getString(Res.string.color_indigo)).performClick()

        // Then
        assertEquals(
            expected = listOf(CodeStyle(darkColor = CodeDotColor.INDIGO.argb)),
            actual = styles,
        )
    }

    @Test
    fun `the colors chosen are the selected ones`() = runComposeUiTest {
        // When
        setContent {
            codeStyleFields(
                style = CodeStyle(
                    darkColor = CodeDotColor.INDIGO.argb,
                    lightColor = CodeBackgroundColor.CREAM.argb,
                ),
            )
        }

        // Then
        onNodeWithContentDescription(getString(Res.string.color_indigo)).assertIsSelected()
        onNodeWithContentDescription(getString(Res.string.color_cream)).assertIsSelected()
        onNodeWithContentDescription(getString(Res.string.color_black)).assertIsNotSelected()
    }

    @Test
    fun `colors that read poorly are told and colors that read well are not`() = runComposeUiTest {
        // Given
        val style = CodeStyle(darkColor = 0xFFDDDDDD)
        setContent {
            codeStyleFields(style = style)
        }

        // Then
        onNodeWithText(getString(Res.string.code_low_contrast)).assertExists()
    }

    @Test
    fun `colors of the palette are not told about`() = runComposeUiTest {
        // When
        setContent {
            codeStyleFields(style = CodeStyle())
        }

        // Then
        onNodeWithText(getString(Res.string.code_low_contrast)).assertDoesNotExist()
    }

    @Test
    fun `another color is chosen in a picker that keeps the color until changed`() = runComposeUiTest {
        // Given
        val styles = mutableListOf<CodeStyle>()
        setContent {
            codeStyleFields(
                style = CodeStyle(darkColor = 0xFF00897B),
                onStyleChanged = { style -> styles += style },
            )
        }

        // When
        // The first "Other color" is the one of the dots
        onAllNodesWithContentDescription(getString(Res.string.other_color))[0].assertIsSelected().performClick()
        onNodeWithText(getString(Res.string.choose)).performClick()

        // Then
        assertEquals(
            expected = listOf(CodeStyle(darkColor = 0xFF00897B)),
            actual = styles,
        )
    }

    @Composable
    private fun codeStyleFields(
        style: CodeStyle,
        onStyleChanged: (CodeStyle) -> Unit = {},
    ) {
        SCTheme {
            CodeStyleFields(
                style = style,
                onStyleChanged = onStyleChanged,
                onAddLogoClicked = {},
            )
        }
    }
}

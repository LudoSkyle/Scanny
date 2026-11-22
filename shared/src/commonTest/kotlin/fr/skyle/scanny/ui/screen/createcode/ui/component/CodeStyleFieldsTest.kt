package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.advanced_options
import fr.skyle.scanny.resources.choose
import fr.skyle.scanny.resources.code_low_contrast
import fr.skyle.scanny.resources.color_black
import fr.skyle.scanny.resources.color_cream
import fr.skyle.scanny.resources.color_green
import fr.skyle.scanny.resources.color_indigo
import fr.skyle.scanny.resources.color_red
import fr.skyle.scanny.resources.corner_color_like_dots
import fr.skyle.scanny.resources.gradient_color
import fr.skyle.scanny.resources.gradient_radial
import fr.skyle.scanny.resources.logo_margin_large
import fr.skyle.scanny.resources.logo_shape
import fr.skyle.scanny.resources.other_color
import fr.skyle.scanny.resources.shape_circle
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import fr.skyle.scanny.ui.test.ComposeTest
import fr.skyle.scanny.ui.test.RED_PNG
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

    @Test
    fun `the corners are like the dots until a color is chosen for them in the advanced options`() = runComposeUiTest {
        // Given
        val styles = mutableListOf<CodeStyle>()
        setContent {
            codeStyleFields(
                style = CodeStyle(),
                onStyleChanged = { style -> styles += style },
            )
        }

        // When
        onNodeWithText(getString(Res.string.advanced_options)).performScrollTo().performClick()
        onNodeWithContentDescription(getString(Res.string.corner_color_like_dots)).performScrollTo().assertIsSelected()
        // The second "Red": the first is the one of the dots
        onAllNodesWithContentDescription(getString(Res.string.color_red))[1].performScrollTo().performClick()

        // Then
        assertEquals(
            expected = listOf(CodeStyle(cornerColor = CodeDotColor.RED.argb)),
            actual = styles,
        )
    }

    @Test
    fun `a gradient is chosen in the advanced options and then its second color`() = runComposeUiTest {
        // Given
        var style by mutableStateOf(CodeStyle())
        setContent {
            codeStyleFields(
                style = style,
                onStyleChanged = { changed -> style = changed },
            )
        }
        onNodeWithText(getString(Res.string.advanced_options)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.gradient_color)).assertDoesNotExist()

        // When
        onNodeWithText(getString(Res.string.gradient_radial)).performScrollTo().performClick()
        // The third "Green": the dots, the corners, then the gradient
        onAllNodesWithContentDescription(getString(Res.string.color_green))[2].performScrollTo().performClick()

        // Then
        assertEquals(
            expected = CodeStyle(
                gradient = CodeGradient.RADIAL,
                gradientColor = CodeDotColor.GREEN.argb,
            ),
            actual = style,
        )
    }

    @Test
    fun `the color of the dots is offered once for the corners as theirs`() = runComposeUiTest {
        // When
        setContent {
            codeStyleFields(style = CodeStyle(darkColor = CodeDotColor.INDIGO.argb))
        }
        onNodeWithText(getString(Res.string.advanced_options)).performScrollTo().performClick()

        // Then
        // The indigo of the dots, not again in the corners
        onAllNodesWithContentDescription(getString(Res.string.color_indigo)).assertCountEquals(1)
        onNodeWithContentDescription(getString(Res.string.corner_color_like_dots)).performScrollTo().assertIsSelected()
    }

    @Test
    fun `the shape and the margin of the logo are chosen in the advanced options once there is one`() = runComposeUiTest {
        // Given
        var style by mutableStateOf(CodeStyle())
        setContent {
            codeStyleFields(
                style = style,
                onStyleChanged = { changed -> style = changed },
            )
        }
        onNodeWithText(getString(Res.string.advanced_options)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.logo_shape)).assertDoesNotExist()

        // When
        style = CodeStyle(logo = RED_PNG)
        onNodeWithText(getString(Res.string.shape_circle)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.logo_margin_large)).performScrollTo().performClick()

        // Then
        assertEquals(
            expected = CodeStyle(
                logo = RED_PNG,
                logoShape = CodeShape.CIRCLE,
                logoMargin = CodeLogoMargin.LARGE,
            ),
            actual = style,
        )
    }

    @Composable
    private fun codeStyleFields(
        style: CodeStyle,
        onStyleChanged: (CodeStyle) -> Unit = {},
    ) {
        SCTheme {
            // In a column that scrolls, as in the editor
            Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
                CodeStyleFields(
                    style = style,
                    logoBlocker = null,
                    onStyleChanged = onStyleChanged,
                    onAddLogoClicked = {},
                )
            }
        }
    }
}

package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.zoom_ratio
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ZoomRatioButtonTest : ComposeTest() {

    @Test
    fun `a zoom shows to the tenth and goes back to 1x when touched`() = runComposeUiTest {
        // Given
        var resetCount = 0
        setContent {
            SCTheme {
                ZoomRatioButton(
                    zoomRatio = 2.46f,
                    onClick = { resetCount++ },
                )
            }
        }

        // When
        onNodeWithText(getString(Res.string.zoom_ratio, 2, 5)).performClick()

        // Then
        assertEquals(
            expected = 1,
            actual = resetCount,
        )
    }

    @Test
    fun `the ultra wide angle camera shows under 1x`() = runComposeUiTest {
        // Given
        setContent {
            SCTheme {
                ZoomRatioButton(
                    zoomRatio = 0.5f,
                    onClick = {},
                )
            }
        }

        // Then
        onNodeWithText(getString(Res.string.zoom_ratio, 0, 5)).assertExists()
    }
}

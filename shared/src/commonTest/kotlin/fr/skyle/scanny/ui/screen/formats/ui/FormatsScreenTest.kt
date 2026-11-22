package fr.skyle.scanny.ui.screen.formats.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.format_ean_13_example
import fr.skyle.scanny.resources.format_ean_13_use
import fr.skyle.scanny.resources.format_read_and_created
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class FormatsScreenTest : ComposeTest() {

    @Test
    fun `every format scanny reads is listed`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                FormatsScreen(onBackClicked = {})
            }
        }

        // Then
        BarcodeFormat.entries.forEach { format ->
            onNode(hasScrollAction()).performScrollToNode(hasText(getString(format.toText())))
            onNodeWithText(getString(format.toText())).assertExists()
        }
    }

    @Test
    fun `a format unfolds into what scanny does with it its use and its example`() = runComposeUiTest {
        // Given
        setContent {
            SCTheme {
                FormatsScreen(onBackClicked = {})
            }
        }
        onNodeWithText(getString(Res.string.format_ean_13_use)).assertDoesNotExist()

        // When
        onNode(hasScrollAction()).performScrollToNode(hasText(getString(BarcodeFormat.EAN_13.toText())))
        onNodeWithText(getString(BarcodeFormat.EAN_13.toText())).performClick()

        // Then
        onNodeWithText(getString(Res.string.format_read_and_created)).assertExists()
        onNodeWithText(getString(Res.string.format_ean_13_use)).assertExists()
        onNodeWithText(getString(Res.string.format_ean_13_example)).assertExists()
    }

    @Test
    fun `a code in two dimensions other than the qr code says scanny creates it too`() = runComposeUiTest {
        // Given
        setContent {
            SCTheme {
                FormatsScreen(onBackClicked = {})
            }
        }

        // When
        onNodeWithText(getString(BarcodeFormat.AZTEC.toText())).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.format_read_and_created)).assertExists()
    }
}

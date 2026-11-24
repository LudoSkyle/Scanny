package fr.skyle.scanny.ui.core.code.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.export_svg
import fr.skyle.scanny.resources.save_image
import fr.skyle.scanny.resources.share_image
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CodeImageSheetTest : ComposeTest() {

    @Test
    fun `the code shows with the actions on its image`() = runComposeUiTest {
        // Given
        val actions = mutableListOf<String>()
        setContent {
            codeImageSheet(
                canSave = true,
                onShareClicked = { actions += "share" },
                onSaveClicked = { actions += "save" },
                onExportSvgClicked = { actions += "svg" },
            )
        }

        // When
        onNodeWithText(getString(Res.string.share_image)).performClick()
        onNodeWithText(getString(Res.string.save_image)).performClick()
        onNodeWithText(getString(Res.string.export_svg)).performClick()

        // Then
        onNodeWithContentDescription(getString(Res.string.the_code)).assertExists()
        assertEquals(
            expected = listOf("share", "save", "svg"),
            actual = actions,
        )
    }

    @Test
    fun `a message about the image shows in the sheet with its action`() = runComposeUiTest {
        // Given
        var isActionClicked: Boolean? = null
        setContent {
            val snackbarState = rememberSCSnackbarState()
            LaunchedEffect(Unit) {
                isActionClicked = snackbarState.show(
                    message = "Image saved to the photos",
                    actionLabel = "See",
                )
            }
            codeImageSheet(
                canSave = true,
                snackbarState = snackbarState,
            )
        }

        // When
        onNodeWithText("Image saved to the photos").assertExists()
        onNodeWithText("See").performClick()

        // Then
        // The message answers once its coroutine resumes, after the touch
        waitUntil { isActionClicked != null }
        assertEquals(
            expected = true,
            actual = isActionClicked,
        )
    }

    @Test
    fun `saving is not offered when the photos do not allow it`() = runComposeUiTest {
        // When
        setContent {
            codeImageSheet(canSave = false)
        }

        // Then
        onNodeWithText(getString(Res.string.share_image)).assertExists()
        onNodeWithText(getString(Res.string.save_image)).assertDoesNotExist()
    }

    @Composable
    private fun codeImageSheet(
        canSave: Boolean,
        snackbarState: SCSnackbarState = rememberSCSnackbarState(),
        onShareClicked: () -> Unit = {},
        onSaveClicked: () -> Unit = {},
        onExportSvgClicked: () -> Unit = {},
    ) {
        SCTheme {
            CodeImageSheet(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
                style = CodeStyle(),
                canSave = canSave,
                snackbarState = snackbarState,
                onShareClicked = onShareClicked,
                onSaveClicked = onSaveClicked,
                onExportSvgClicked = onExportSvgClicked,
                onDismissRequest = {},
            )
        }
    }
}

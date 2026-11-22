package fr.skyle.scanny.ui.core.code.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.image_saved_short
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
                isSaved = false,
                onShareClicked = { actions += "share" },
                onSaveClicked = { actions += "save" },
            )
        }

        // When
        onNodeWithText(getString(Res.string.share_image)).performClick()
        onNodeWithText(getString(Res.string.save_image)).performClick()

        // Then
        onNodeWithContentDescription(getString(Res.string.the_code)).assertExists()
        assertEquals(
            expected = listOf("share", "save"),
            actual = actions,
        )
    }

    @Test
    fun `a saved image is told by its button`() = runComposeUiTest {
        // When
        setContent {
            codeImageSheet(
                canSave = true,
                isSaved = true,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.image_saved_short)).assertExists()
    }

    @Test
    fun `saving is not offered when the photos do not allow it`() = runComposeUiTest {
        // When
        setContent {
            codeImageSheet(
                canSave = false,
                isSaved = false,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.share_image)).assertExists()
        onNodeWithText(getString(Res.string.save_image)).assertDoesNotExist()
    }

    @Composable
    private fun codeImageSheet(
        canSave: Boolean,
        isSaved: Boolean,
        onShareClicked: () -> Unit = {},
        onSaveClicked: () -> Unit = {},
    ) {
        SCTheme {
            CodeImageSheet(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
                style = CodeStyle(),
                canSave = canSave,
                isSaved = isSaved,
                onShareClicked = onShareClicked,
                onSaveClicked = onSaveClicked,
                onDismissRequest = {},
            )
        }
    }
}

package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.resources.content_size
import fr.skyle.scanny.resources.content_size_count
import fr.skyle.scanny.resources.content_size_wide_characters
import fr.skyle.scanny.resources.create
import fr.skyle.scanny.resources.create_code
import fr.skyle.scanny.resources.customization
import fr.skyle.scanny.resources.next
import fr.skyle.scanny.resources.step_of
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.resources.value_not_for_format
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CreateCodeScreenTest : ComposeTest() {

    @Test
    fun `the content of a qr code is the first of two steps and goes on to its customization`() = runComposeUiTest {
        // Given
        var nextClickCount = 0
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = CreationStep.CONTENT,
                onNextClicked = { nextClickCount += 1 },
            )
        }

        // When
        onNodeWithText(getString(Res.string.next)).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.content)).assertExists()
        onNodeWithText(getString(Res.string.step_of, 1, 2)).assertExists()
        assertEquals(
            expected = 1,
            actual = nextClickCount,
        )
    }

    @Test
    fun `the customization is the second step and creates the code`() = runComposeUiTest {
        // Given
        var createClickCount = 0
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = CreationStep.STYLE,
                onCreateClicked = { createClickCount += 1 },
            )
        }

        // When
        onNodeWithText(getString(Res.string.create)).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.customization)).assertExists()
        onNodeWithText(getString(Res.string.step_of, 2, 2)).assertExists()
        assertEquals(
            expected = 1,
            actual = createClickCount,
        )
    }

    @Test
    fun `a barcode is created in a single step from its content`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    kind = CreationKind.BARCODE,
                    barcodeFormat = BarcodeFormat.EAN_13,
                    barcodeValue = "4006381333931",
                ),
                preview = CodePreview.Code(rawValue = "4006381333931", format = BarcodeFormat.EAN_13),
                step = CreationStep.CONTENT,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.create_code)).assertExists()
        onNodeWithText(getString(Res.string.create)).assertExists()
        onNodeWithText(getString(Res.string.next)).assertDoesNotExist()
    }

    @Test
    fun `a content that is incomplete cannot go on`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(),
                preview = CodePreview.Incomplete,
                step = CreationStep.CONTENT,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.next)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `a link shows its field and the code it makes`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    kind = CreationKind.LINK,
                    url = "scanny.app",
                ),
                preview = CodePreview.Code(rawValue = "https://scanny.app", format = BarcodeFormat.QR_CODE),
                step = CreationStep.CONTENT,
            )
        }

        // Then
        onNodeWithText("scanny.app").assertExists()
        onNodeWithContentDescription(getString(Res.string.the_code)).assertExists()
    }

    @Test
    fun `a barcode its format refuses says so and cannot be created`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    kind = CreationKind.BARCODE,
                    barcodeFormat = BarcodeFormat.EAN_13,
                    barcodeValue = "4006381333932",
                ),
                preview = CodePreview.Refused(format = BarcodeFormat.EAN_13),
                step = CreationStep.CONTENT,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.value_not_for_format)).assertExists()
        onNodeWithText(getString(Res.string.create)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `the size of the whole content is counted under its fields`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    kind = CreationKind.LINK,
                    url = "scanny.app",
                ),
                preview = CodePreview.Code(rawValue = "https://scanny.app", format = BarcodeFormat.QR_CODE),
                step = CreationStep.CONTENT,
                contentSize = ContentSize(
                    used = 18,
                    max = 1273,
                    hasWideCharacters = false,
                ),
            )
        }

        // Then
        onNodeWithText(getString(Res.string.content_size)).assertExists()
        onNodeWithText(getString(Res.string.content_size_count, 18, 1273)).assertExists()
        onNodeWithText(getString(Res.string.content_size_wide_characters)).assertDoesNotExist()
    }

    @Test
    fun `a content with an accent or an emoji says they count for several`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Café"),
                preview = CodePreview.Code(rawValue = "Café", format = BarcodeFormat.QR_CODE),
                step = CreationStep.CONTENT,
                contentSize = ContentSize(
                    used = 5,
                    max = 1273,
                    hasWideCharacters = true,
                ),
            )
        }

        // Then
        onNodeWithText(getString(Res.string.content_size_wide_characters)).assertExists()
    }

    @Composable
    private fun createCodeScreen(
        form: CreationForm,
        preview: CodePreview,
        step: CreationStep,
        contentSize: ContentSize? = null,
        onNextClicked: () -> Unit = {},
        onCreateClicked: () -> Unit = {},
    ) {
        SCTheme {
            CreateCodeScreen(
                form = form,
                preview = preview,
                contentSize = contentSize,
                style = CodeStyle(),
                step = step,
                isCreating = false,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onFormChanged = {},
                onStyleChanged = {},
                onAddLogoClicked = {},
                onNextClicked = onNextClicked,
                onCreateClicked = onCreateClicked,
            )
        }
    }
}

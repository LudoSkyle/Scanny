package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_logo
import fr.skyle.scanny.resources.advanced_options
import fr.skyle.scanny.resources.aztec
import fr.skyle.scanny.resources.barcode_value
import fr.skyle.scanny.resources.characters_not_held
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.resources.content_size
import fr.skyle.scanny.resources.content_size_count
import fr.skyle.scanny.resources.content_size_wide_characters
import fr.skyle.scanny.resources.content_type
import fr.skyle.scanny.resources.create
import fr.skyle.scanny.resources.create_code
import fr.skyle.scanny.resources.customization
import fr.skyle.scanny.resources.data_matrix
import fr.skyle.scanny.resources.error_correction_auto
import fr.skyle.scanny.resources.error_correction_high
import fr.skyle.scanny.resources.error_correction_medium
import fr.skyle.scanny.resources.error_correction_too_high_for_content
import fr.skyle.scanny.resources.logo_needs_highest_correction
import fr.skyle.scanny.resources.next
import fr.skyle.scanny.resources.reset_customization
import fr.skyle.scanny.resources.step_of
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.resources.two_dimensional_code
import fr.skyle.scanny.resources.value_not_for_format
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeRefusal
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationFamily
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
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
                    family = CreationFamily.BARCODE,
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
    fun `a code in two dimensions offers its formats and its contents and is created in a single step`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    family = CreationFamily.TWO_DIMENSIONAL,
                    kind = CreationKind.LINK,
                    url = "scanny.app",
                ),
                preview = CodePreview.Code(rawValue = "https://scanny.app", format = BarcodeFormat.DATA_MATRIX),
                step = CreationStep.CONTENT,
            )
        }

        // Then
        // The format chosen in a dropdown, the others in its menu
        onNodeWithText(getString(Res.string.data_matrix)).assertExists()
        onNodeWithText(getString(Res.string.aztec)).assertDoesNotExist()
        onNodeWithText("scanny.app").assertExists()
        onNodeWithText(getString(Res.string.create)).performScrollTo().assertExists()
        onNodeWithText(getString(Res.string.next)).assertDoesNotExist()
    }

    @Test
    fun `the family of the code is chosen above its content`() = runComposeUiTest {
        // Given
        var changedForm: CreationForm? = null
        setContent {
            createCodeScreen(
                form = CreationForm(),
                preview = CodePreview.Incomplete,
                step = CreationStep.CONTENT,
                onFormChanged = { form -> changedForm = form },
            )
        }

        // When
        onNodeWithText(getString(Res.string.two_dimensional_code)).performClick()

        // Then
        assertEquals(
            expected = CreationForm(family = CreationFamily.TWO_DIMENSIONAL),
            actual = changedForm,
        )
    }

    @Test
    fun `a barcode has a format and a value but no content type`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(family = CreationFamily.BARCODE),
                preview = CodePreview.Incomplete,
                step = CreationStep.CONTENT,
            )
        }

        // Then
        // Marked as required
        onNodeWithText(getString(Res.string.barcode_value), substring = true).assertExists()
        onAllNodesWithText(getString(Res.string.content_type)).assertCountEquals(0)
    }

    @Test
    fun `a character a format does not hold says so and offers the formats that hold it`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    family = CreationFamily.TWO_DIMENSIONAL,
                    text = "Prix : 5 €",
                ),
                preview = CodePreview.Refused(
                    format = BarcodeFormat.DATA_MATRIX,
                    reason = CodeRefusal.CHARACTERS_NOT_HELD,
                ),
                step = CreationStep.CONTENT,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.characters_not_held)).performScrollTo().assertExists()
        onNodeWithText(getString(Res.string.create)).performScrollTo().assertIsNotEnabled()
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
                    family = CreationFamily.BARCODE,
                    barcodeFormat = BarcodeFormat.EAN_13,
                    barcodeValue = "4006381333932",
                ),
                preview = CodePreview.Refused(
                    format = BarcodeFormat.EAN_13,
                    reason = CodeRefusal.VALUE_NOT_FOR_FORMAT,
                ),
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

    @Test
    fun `the error correction is chosen in the folded advanced options of the content`() = runComposeUiTest {
        // Given
        val styles = mutableListOf<CodeStyle>()
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = CreationStep.CONTENT,
                onStyleChanged = { style -> styles += style },
            )
        }
        onNodeWithText(getString(Res.string.error_correction_medium)).assertDoesNotExist()

        // When
        onNodeWithText(getString(Res.string.advanced_options)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.error_correction_medium)).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.error_correction_auto)).assertIsSelected()
        assertEquals(
            expected = listOf(CodeStyle(errorCorrection = CodeErrorCorrection.MEDIUM)),
            actual = styles,
        )
    }

    @Test
    fun `a correction that cannot be chosen says why`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = CreationStep.CONTENT,
                errorCorrectionChoices = listOf(
                    ErrorCorrectionChoice(
                        errorCorrection = CodeErrorCorrection.AUTO,
                        blocker = null,
                    ),
                    ErrorCorrectionChoice(
                        errorCorrection = CodeErrorCorrection.HIGH,
                        blocker = ErrorCorrectionBlocker.TOO_HIGH_FOR_CONTENT,
                    ),
                ),
            )
        }
        onNodeWithText(getString(Res.string.advanced_options)).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.error_correction_high)).performScrollTo().assertIsNotEnabled()
        onNodeWithText(getString(Res.string.error_correction_too_high_for_content)).assertExists()
    }

    @Test
    fun `a barcode has no advanced options`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(
                    family = CreationFamily.BARCODE,
                    barcodeFormat = BarcodeFormat.EAN_13,
                    barcodeValue = "4006381333931",
                ),
                preview = CodePreview.Code(rawValue = "4006381333931", format = BarcodeFormat.EAN_13),
                step = CreationStep.CONTENT,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.advanced_options)).assertDoesNotExist()
    }

    @Test
    fun `a logo that cannot be added says why`() = runComposeUiTest {
        // When
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = CreationStep.STYLE,
                logoBlocker = LogoBlocker.CORRECTION_TOO_LOW,
            )
        }

        // Then
        onNodeWithText(getString(Res.string.add_logo)).performScrollTo().assertIsNotEnabled()
        onNodeWithText(getString(Res.string.logo_needs_highest_correction)).assertExists()
    }

    @Test
    fun `the customization is reset from the top bar of its step`() = runComposeUiTest {
        // Given
        var resetCount = 0
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = CreationStep.STYLE,
                canResetStyle = true,
                onResetStyleClicked = { resetCount++ },
            )
        }

        // When
        onNodeWithContentDescription(getString(Res.string.reset_customization)).performClick()

        // Then
        assertEquals(
            expected = 1,
            actual = resetCount,
        )
    }

    @Test
    fun `a default customization has nothing to reset and the content has no reset`() = runComposeUiTest {
        // Given
        var step by mutableStateOf(CreationStep.STYLE)
        setContent {
            createCodeScreen(
                form = CreationForm(text = "Hello"),
                preview = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
                step = step,
                canResetStyle = false,
            )
        }
        onNodeWithContentDescription(getString(Res.string.reset_customization)).assertIsNotEnabled()

        // When
        step = CreationStep.CONTENT

        // Then
        onNodeWithContentDescription(getString(Res.string.reset_customization)).assertDoesNotExist()
    }

    @Composable
    private fun createCodeScreen(
        form: CreationForm,
        preview: CodePreview,
        step: CreationStep,
        contentSize: ContentSize? = null,
        errorCorrectionChoices: List<ErrorCorrectionChoice> = CodeErrorCorrection.entries.map { errorCorrection ->
            ErrorCorrectionChoice(
                errorCorrection = errorCorrection,
                blocker = null,
            )
        },
        logoBlocker: LogoBlocker? = null,
        canResetStyle: Boolean = false,
        onFormChanged: (CreationForm) -> Unit = {},
        onStyleChanged: (CodeStyle) -> Unit = {},
        onResetStyleClicked: () -> Unit = {},
        onNextClicked: () -> Unit = {},
        onCreateClicked: () -> Unit = {},
    ) {
        SCTheme {
            CreateCodeScreen(
                form = form,
                preview = preview,
                contentSize = contentSize,
                errorCorrectionChoices = errorCorrectionChoices,
                logoBlocker = logoBlocker,
                style = CodeStyle(),
                canResetStyle = canResetStyle,
                step = step,
                isCreating = false,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onFormChanged = onFormChanged,
                onStyleChanged = onStyleChanged,
                onResetStyleClicked = onResetStyleClicked,
                onAddLogoClicked = {},
                onNextClicked = onNextClicked,
                onCreateClicked = onCreateClicked,
            )
        }
    }
}

package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.copied
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.see_detail
import fr.skyle.scanny.resources.upc_a
import fr.skyle.scanny.resources.write_sms
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ScanResultSheetTest : ComposeTest() {

    @Test
    fun `an sms shows its number and its message and is written by its button`() = runComposeUiTest {
        // Given
        val actions = mutableListOf<CodeAction>()
        setContent {
            scanResultSheet(
                result = SMS,
                onActionClicked = { action -> actions += action },
            )
        }

        // When
        onNodeWithText(getString(Res.string.write_sms)).performClick()

        // Then
        onNodeWithText("+33600000000").assertExists()
        onNodeWithText("See you at 10:30 & bring été").assertExists()
        assertEquals(
            expected = listOf(SMS.action),
            actual = actions,
        )
    }

    @Test
    fun `the code is copied and its detail opened from the sheet`() = runComposeUiTest {
        // Given
        val clicks = mutableListOf<String>()
        setContent {
            scanResultSheet(
                result = SMS,
                onCopyClicked = { clicks += "copy" },
                onSeeDetailClicked = { clicks += "detail" },
            )
        }

        // When
        onNodeWithText(getString(Res.string.copy)).performClick()
        onNodeWithText(getString(Res.string.see_detail)).performClick()

        // Then
        // iOS shows nothing when a text is copied: the button tells it
        onNodeWithText(getString(Res.string.copied)).assertExists()
        assertEquals(
            expected = listOf("copy", "detail"),
            actual = clicks,
        )
    }

    @Test
    fun `a barcode shows its format`() = runComposeUiTest {
        // When
        setContent {
            scanResultSheet(
                result = ScanResultItem(
                    codeId = 2,
                    type = CodeType.TEXT,
                    format = BarcodeFormat.UPC_A,
                    fields = listOf(
                        CodeField(
                            label = null,
                            value = "123456789012",
                        ),
                    ),
                    rawValue = "123456789012",
                    action = null,
                ),
            )
        }

        // Then
        onNodeWithText(getString(Res.string.upc_a)).assertExists()
        onNodeWithText("123456789012").assertExists()
    }

    @Composable
    private fun scanResultSheet(
        result: ScanResultItem,
        onActionClicked: (CodeAction) -> Unit = {},
        onCopyClicked: () -> Unit = {},
        onSeeDetailClicked: () -> Unit = {},
    ) {
        SCTheme {
            ScanResultSheet(
                result = result,
                onActionClicked = onActionClicked,
                onCopyClicked = onCopyClicked,
                onSeeDetailClicked = onSeeDetailClicked,
                onDismissRequest = {},
            )
        }
    }

    private companion object {
        val SMS = ScanResultItem(
            codeId = 1,
            type = CodeType.SMS,
            format = BarcodeFormat.QR_CODE,
            fields = listOf(
                CodeField(
                    label = CodeFieldLabel.PHONE_NUMBER,
                    value = "+33600000000",
                ),
                CodeField(
                    label = CodeFieldLabel.MESSAGE,
                    value = "See you at 10:30 & bring été",
                ),
            ),
            rawValue = "SMSTO:+33600000000:See you at 10:30 & bring été",
            action = CodeAction.WriteSms(link = "sms:+33600000000"),
        )
    }
}

package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_count
import fr.skyle.scanny.resources.discard
import fr.skyle.scanny.resources.discard_codes_question
import fr.skyle.scanny.resources.see_group
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.test.ComposeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalTestApi::class)
internal class ScanGroupResultSheetTest : ComposeTest() {

    @Test
    fun `a code of the group opens its detail and the group opens from its button`() = runComposeUiTest {
        // Given
        val clicks = mutableListOf<String>()
        setContent {
            SCTheme {
                ScanGroupResultSheet(
                    codes = listOf(
                        CodeRowItem(
                            codeId = 1,
                            type = CodeType.TEXT,
                            summary = "First of two",
                            format = BarcodeFormat.QR_CODE,
                        ),
                        CodeRowItem(
                            codeId = 2,
                            type = CodeType.TEXT,
                            summary = "Second of two",
                            format = BarcodeFormat.QR_CODE,
                        ),
                    ),
                    onCodeClicked = { codeId -> clicks += "code $codeId" },
                    onSeeGroupClicked = { clicks += "group" },
                    onDismissRequest = {},
                    isFavorite = false,
                    onFavoriteClicked = {},
                )
            }
        }

        // When
        onNodeWithText("Second of two").performClick()
        onNodeWithText(getString(Res.string.see_group)).performClick()

        // Then
        onNodeWithText(getString(Res.string.code_count, 2)).assertExists()
        onNodeWithText("First of two").assertExists()
        assertEquals(
            expected = listOf("code 2", "group"),
            actual = clicks,
        )
    }

    @Test
    fun `the codes found are discarded once confirmed`() = runComposeUiTest {
        // Given
        var discardCount = 0
        setContent {
            SCTheme {
                DiscardCodesDialog(
                    onConfirmClicked = { discardCount++ },
                    onDismissRequest = {},
                )
            }
        }
        onNodeWithText(getString(Res.string.discard_codes_question)).assertExists()

        // When
        onNodeWithText(getString(Res.string.discard)).performClick()

        // Then
        assertEquals(
            expected = 1,
            actual = discardCount,
        )
    }
}

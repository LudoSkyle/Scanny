package fr.skyle.scanny.ui.core.code.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.format_ean_13_example
import fr.skyle.scanny.resources.format_ean_13_structure
import fr.skyle.scanny.resources.format_ean_13_use
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class CodeInfoSheetTest : ComposeTest() {

    @Test
    fun `the sheet of a format tells its use how it is made and an example of it`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                CodeInfoSheet(
                    format = BarcodeFormat.EAN_13,
                    onDismissRequest = {},
                )
            }
        }

        // Then
        onNodeWithText(getString(Res.string.format_ean_13_use)).assertExists()
        onNodeWithText(getString(Res.string.format_ean_13_structure)).assertExists()
        onNodeWithText(getString(Res.string.format_ean_13_example)).assertExists()
    }
}

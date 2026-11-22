package fr.skyle.scanny.ui.core.code.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.format
import fr.skyle.scanny.resources.format_ean_13_info
import fr.skyle.scanny.resources.type_wifi_info
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class CodeInfoSheetTest : ComposeTest() {

    @Test
    fun `the sheet tells what the type and the format of the code are`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                CodeInfoSheet(
                    type = CodeType.WIFI,
                    format = BarcodeFormat.EAN_13,
                    onDismissRequest = {},
                )
            }
        }

        // Then
        onNodeWithText(getString(Res.string.type_wifi_info)).assertExists()
        onNodeWithText(getString(Res.string.format_ean_13_info)).assertExists()
    }

    @Test
    fun `a code with no format known tells its type only`() = runComposeUiTest {
        // When
        setContent {
            SCTheme {
                CodeInfoSheet(
                    type = CodeType.WIFI,
                    format = null,
                    onDismissRequest = {},
                )
            }
        }

        // Then
        onNodeWithText(getString(Res.string.type_wifi_info)).assertExists()
        onNodeWithText(getString(Res.string.format)).assertDoesNotExist()
    }
}

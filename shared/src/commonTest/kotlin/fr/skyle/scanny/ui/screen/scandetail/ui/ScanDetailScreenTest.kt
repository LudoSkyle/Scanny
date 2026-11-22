package fr.skyle.scanny.ui.screen.scandetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.edit_code
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.raw_content
import fr.skyle.scanny.resources.recipient
import fr.skyle.scanny.resources.share
import fr.skyle.scanny.resources.show_code
import fr.skyle.scanny.resources.write_email
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ScanDetailScreenTest : ComposeTest() {

    @Test
    fun `an e-mail shows its recipient and its subject and is written by its button`() = runComposeUiTest {
        // Given
        val actions = mutableListOf<CodeAction>()
        setContent {
            scanDetailScreen(
                code = EMAIL,
                onActionClicked = { action -> actions += action },
            )
        }

        // When
        onNodeWithText(getString(Res.string.write_email)).performScrollTo().performClick()

        // Then
        onNodeWithText(getString(Res.string.recipient)).assertExists()
        onNodeWithText("john@doe.fr").assertExists()
        onNodeWithText("Hello Scanny").assertExists()
        assertEquals(
            expected = listOf(EMAIL.action),
            actual = actions,
        )
    }

    @Test
    fun `a field is copied by its button`() = runComposeUiTest {
        // Given
        val copies = mutableListOf<String>()
        setContent {
            scanDetailScreen(
                code = EMAIL,
                onCopyClicked = { text -> copies += text },
            )
        }

        // When
        onAllNodesWithContentDescription(getString(Res.string.copy))[0].performClick()

        // Then
        assertEquals(
            expected = listOf("john@doe.fr"),
            actual = copies,
        )
    }

    @Test
    fun `the raw content is folded under its title and unfolded by a touch`() = runComposeUiTest {
        // Given
        setContent {
            scanDetailScreen(code = WIFI)
        }
        onNodeWithText(getString(Res.string.password)).assertExists()
        onNodeWithText(WIFI.rawValue).assertDoesNotExist()

        // When
        onNodeWithText(getString(Res.string.raw_content)).performScrollTo().performClick()

        // Then
        onNodeWithText(WIFI.rawValue).assertExists()
    }

    @Test
    fun `a code drawn again is shown and edited from the top bar`() = runComposeUiTest {
        // Given
        val clicks = mutableListOf<String>()
        setContent {
            scanDetailScreen(
                code = EMAIL,
                onShowCodeClicked = { clicks += "show" },
                onEditClicked = { clicks += "edit" },
            )
        }

        // When
        onNodeWithContentDescription(getString(Res.string.show_code)).performClick()
        onNodeWithContentDescription(getString(Res.string.edit_code)).performClick()

        // Then
        assertEquals(
            expected = listOf("show", "edit"),
            actual = clicks,
        )
    }

    @Test
    fun `a code that cannot be drawn again has no button to show it`() = runComposeUiTest {
        // When
        setContent {
            scanDetailScreen(code = EMAIL.copy(format = BarcodeFormat.DATA_MATRIX))
        }

        // Then
        onNodeWithContentDescription(getString(Res.string.share)).assertExists()
        onNodeWithContentDescription(getString(Res.string.show_code)).assertDoesNotExist()
    }

    @Composable
    private fun scanDetailScreen(
        code: ScanDetailItem,
        onShowCodeClicked: () -> Unit = {},
        onEditClicked: () -> Unit = {},
        onCopyClicked: (String) -> Unit = {},
        onActionClicked: (CodeAction) -> Unit = {},
    ) {
        SCTheme {
            ScanDetailScreen(
                code = code,
                isDeleting = false,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onShowCodeClicked = onShowCodeClicked,
                onEditClicked = onEditClicked,
                onShareClicked = {},
                onDeleteClicked = {},
                onCopyClicked = onCopyClicked,
                onActionClicked = onActionClicked,
            )
        }
    }

    private companion object {
        val EMAIL = ScanDetailItem(
            type = CodeType.EMAIL,
            format = BarcodeFormat.QR_CODE,
            scannedDay = CalendarDay.Today,
            scannedTime = "21:48",
            fields = listOf(
                CodeField(
                    label = CodeFieldLabel.RECIPIENT,
                    value = "john@doe.fr",
                ),
                CodeField(
                    label = CodeFieldLabel.SUBJECT,
                    value = "Hello Scanny",
                ),
            ),
            rawValue = "mailto:john@doe.fr?subject=Hello%20Scanny",
            isRawValueShown = true,
            action = CodeAction.WriteEmail(link = "mailto:john@doe.fr?subject=Hello%20Scanny"),
        )
        val WIFI = ScanDetailItem(
            type = CodeType.WIFI,
            format = BarcodeFormat.QR_CODE,
            scannedDay = CalendarDay.Today,
            scannedTime = "21:48",
            fields = listOf(
                CodeField(
                    label = CodeFieldLabel.NETWORK,
                    value = "ScannyNet",
                ),
                CodeField(
                    label = CodeFieldLabel.PASSWORD,
                    value = "secret123",
                ),
            ),
            rawValue = "WIFI:T:WPA;S:ScannyNet;P:secret123;;",
            isRawValueShown = true,
            action = null,
        )
    }
}

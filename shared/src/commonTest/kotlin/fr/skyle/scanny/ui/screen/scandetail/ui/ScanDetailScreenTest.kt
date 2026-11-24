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
import fr.skyle.scanny.resources.about_format
import fr.skyle.scanny.resources.add_to_favorites
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.edit_code
import fr.skyle.scanny.resources.hidden_password
import fr.skyle.scanny.resources.hide_password
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.raw_content
import fr.skyle.scanny.resources.recipient
import fr.skyle.scanny.resources.remove_from_favorites
import fr.skyle.scanny.resources.share
import fr.skyle.scanny.resources.show_code
import fr.skyle.scanny.resources.show_password
import fr.skyle.scanny.resources.write_email
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import fr.skyle.scanny.ui.test.ComposeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString

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
    fun `a password is hidden until touched and copied whole`() = runComposeUiTest {
        // Given
        val copies = mutableListOf<String>()
        setContent {
            scanDetailScreen(
                code = WIFI,
                onCopyClicked = { text -> copies += text },
            )
        }
        onNodeWithText(getString(Res.string.hidden_password)).assertExists()
        onNodeWithText("secret123").assertDoesNotExist()

        // When
        onNodeWithContentDescription(getString(Res.string.show_password)).performClick()
        onAllNodesWithContentDescription(getString(Res.string.copy))[1].performClick()

        // Then
        onNodeWithText("secret123").assertExists()
        onNodeWithContentDescription(getString(Res.string.hide_password)).assertExists()
        assertEquals(
            expected = listOf("secret123"),
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
    fun `the i next to the format opens what the format is`() = runComposeUiTest {
        // Given
        var infoClickCount = 0
        setContent {
            scanDetailScreen(
                code = EMAIL,
                onFormatInfoClicked = { infoClickCount++ },
            )
        }

        // When
        onNodeWithContentDescription(getString(Res.string.about_format)).performClick()

        // Then
        assertEquals(
            expected = 1,
            actual = infoClickCount,
        )
    }

    @Test
    fun `a code with no format known has no i`() = runComposeUiTest {
        // When
        setContent {
            scanDetailScreen(code = EMAIL.copy(format = null))
        }

        // Then
        onNodeWithContentDescription(getString(Res.string.about_format)).assertDoesNotExist()
    }

    @Test
    fun `a code of an unknown format has no button to show it`() = runComposeUiTest {
        // When
        setContent {
            scanDetailScreen(code = EMAIL.copy(format = null))
        }

        // Then
        onNodeWithContentDescription(getString(Res.string.share)).assertExists()
        onNodeWithContentDescription(getString(Res.string.show_code)).assertDoesNotExist()
    }

    @Test
    fun `a data matrix has a button to show it`() = runComposeUiTest {
        // When
        setContent {
            scanDetailScreen(code = EMAIL.copy(format = BarcodeFormat.DATA_MATRIX))
        }

        // Then
        onNodeWithContentDescription(getString(Res.string.show_code)).assertExists()
    }

    @Test
    fun `the star puts a single code among the favorites and shows when it is one`() = runComposeUiTest {
        // Given
        var favoriteClickCount = 0
        setContent {
            scanDetailScreen(
                code = EMAIL.copy(isFavorite = true),
                onFavoriteClicked = { favoriteClickCount++ },
            )
        }

        // When
        onNodeWithContentDescription(getString(Res.string.remove_from_favorites)).performClick()

        // Then
        assertEquals(
            expected = 1,
            actual = favoriteClickCount,
        )
    }

    @Test
    fun `a code of a group has no star of its own`() = runComposeUiTest {
        // When
        setContent {
            scanDetailScreen(code = EMAIL.copy(isInGroup = true))
        }

        // Then
        onNodeWithContentDescription(getString(Res.string.add_to_favorites)).assertDoesNotExist()
    }

    @Composable
    private fun scanDetailScreen(
        code: ScanDetailItem,
        onShowCodeClicked: () -> Unit = {},
        onFormatInfoClicked: () -> Unit = {},
        onEditClicked: () -> Unit = {},
        onCopyClicked: (String) -> Unit = {},
        onActionClicked: (CodeAction) -> Unit = {},
        onFavoriteClicked: () -> Unit = {},
    ) {
        SCTheme {
            ScanDetailScreen(
                code = code,
                isDeleting = false,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onShowCodeClicked = onShowCodeClicked,
                onFormatInfoClicked = onFormatInfoClicked,
                onEditClicked = onEditClicked,
                onShareClicked = {},
                onDeleteClicked = {},
                onCopyClicked = onCopyClicked,
                onActionClicked = onActionClicked,
                onFavoriteClicked = onFavoriteClicked,
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
            scanId = 1,
            isFavorite = false,
            isInGroup = false,
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
            scanId = 1,
            isFavorite = false,
            isInGroup = false,
        )
    }
}

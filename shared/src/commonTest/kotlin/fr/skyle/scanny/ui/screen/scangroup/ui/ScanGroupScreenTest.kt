package fr.skyle.scanny.ui.screen.scangroup.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.aztec
import fr.skyle.scanny.resources.code_count
import fr.skyle.scanny.resources.codes_of_group
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.scangroup.ui.model.ScanGroupItem
import fr.skyle.scanny.ui.test.ComposeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalTestApi::class)
internal class ScanGroupScreenTest : ComposeTest() {

    @Test
    fun `the group stands apart from its codes drawn as in the history which open their detail`() = runComposeUiTest {
        // Given
        val clicked = mutableListOf<Long>()
        setContent {
            SCTheme {
                ScanGroupScreen(
                    group = ScanGroupItem(
                        scannedDay = CalendarDay.Today,
                        scannedTime = "21:48",
                        codes = listOf(
                            CodeRowItem(
                                codeId = 1,
                                type = CodeType.LINK,
                                summary = "https://scanny.app",
                                format = BarcodeFormat.QR_CODE,
                            ),
                            CodeRowItem(
                                codeId = 2,
                                type = CodeType.WIFI,
                                summary = "ScannyNet",
                                format = BarcodeFormat.AZTEC,
                            ),
                            CodeRowItem(
                                codeId = 3,
                                type = CodeType.TEXT,
                                summary = "A text of the group",
                                format = BarcodeFormat.QR_CODE,
                            ),
                        ),
                        isFavorite = false,
                    ),
                    snackbarState = rememberSCSnackbarState(),
                    onBackClicked = {},
                    onShareClicked = {},
                    onDeleteClicked = {},
                    onCodeClicked = { codeId -> clicked += codeId },
                    onFavoriteClicked = {},
                )
            }
        }

        // When
        onNodeWithText("ScannyNet").performScrollTo().performClick()

        // Then
        // The group in its card, its codes under their own title
        onNodeWithText(getString(Res.string.code_count, 3)).assertExists()
        onNodeWithText(getString(Res.string.codes_of_group)).assertExists()
        // Each code by its format, its type drawn
        onNodeWithText(getString(Res.string.aztec)).assertExists()
        assertEquals(
            expected = listOf(2L),
            actual = clicked,
        )
    }
}

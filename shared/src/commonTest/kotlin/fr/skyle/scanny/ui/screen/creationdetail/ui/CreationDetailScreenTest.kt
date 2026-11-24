package fr.skyle.scanny.ui.screen.creationdetail.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.edit_code
import fr.skyle.scanny.resources.show_code
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CreationDetailScreenTest : ComposeTest() {

    @Test
    fun `a creation shows its content and is shown and edited from the top bar`() = runComposeUiTest {
        // Given
        val clicks = mutableListOf<String>()
        setContent {
            SCTheme {
                CreationDetailScreen(
                    creation = CreationDetailItem(
                        type = CodeType.TEXT,
                        format = BarcodeFormat.QR_CODE,
                        createdDay = CalendarDay.Today,
                        createdTime = "21:48",
                        fields = listOf(
                            CodeField(
                                label = null,
                                value = "Created for the history",
                            ),
                        ),
                        rawValue = "Created for the history",
                        isRawValueShown = false,
                        style = CodeStyle(),
                        isFavorite = false,
                    ),
                    isDeleting = false,
                    snackbarState = rememberSCSnackbarState(),
                    onBackClicked = {},
                    onShowCodeClicked = { clicks += "show" },
                    onFormatInfoClicked = {},
                    onEditClicked = { clicks += "edit" },
                    onDeleteClicked = {},
                    onCopyClicked = {},
                    onFavoriteClicked = {},
                )
            }
        }

        // When
        onNodeWithContentDescription(getString(Res.string.show_code)).performClick()
        onNodeWithContentDescription(getString(Res.string.edit_code)).performClick()

        // Then
        onNodeWithText("Created for the history").assertExists()
        assertEquals(
            expected = listOf("show", "edit"),
            actual = clicks,
        )
    }
}

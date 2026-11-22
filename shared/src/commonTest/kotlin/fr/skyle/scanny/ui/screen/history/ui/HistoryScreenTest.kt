package fr.skyle.scanny.ui.screen.history.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.all_creations_deleted
import fr.skyle.scanny.resources.all_scans_deleted
import fr.skyle.scanny.resources.clear
import fr.skyle.scanny.resources.clear_history
import fr.skyle.scanny.resources.clear_history_question
import fr.skyle.scanny.resources.creations
import fr.skyle.scanny.resources.group_of_codes
import fr.skyle.scanny.resources.no_creation_yet
import fr.skyle.scanny.resources.no_scan_yet
import fr.skyle.scanny.resources.scans
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.history.ui.component.ClearHistoryDialog
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryView
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistorySection
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class HistoryScreenTest : ComposeTest() {

    @Test
    fun `a scan opens from its row and a group tells its number of codes`() = runComposeUiTest {
        // Given
        val clicked = mutableListOf<ScanHistoryItem>()
        setContent {
            historyScreen(
                selectedView = HistoryView.SCANS,
                onScanClicked = { item -> clicked += item },
            )
        }

        // When
        onNodeWithText(LINK.summary).performClick()

        // Then
        onNodeWithText(getString(Res.string.group_of_codes, 3)).assertExists()
        assertEquals(
            expected = listOf(LINK),
            actual = clicked,
        )
    }

    @Test
    fun `a creation opens from its row`() = runComposeUiTest {
        // Given
        val clicked = mutableListOf<CreationHistoryItem>()
        setContent {
            historyScreen(
                selectedView = HistoryView.CREATIONS,
                onCreationClicked = { item -> clicked += item },
            )
        }

        // When
        onNodeWithText(CREATION.summary).performClick()

        // Then
        assertEquals(
            expected = listOf(CREATION),
            actual = clicked,
        )
    }

    @Test
    fun `the views follow the finger and the switch follows the view`() = runComposeUiTest {
        // Given
        var selectedView by mutableStateOf(HistoryView.SCANS)
        setContent {
            historyScreen(
                selectedView = selectedView,
                onViewSelected = { view -> selectedView = view },
            )
        }

        // When
        onRoot().performTouchInput { swipeLeft() }

        // Then
        onNodeWithText(getString(Res.string.creations)).assertIsSelected()
        assertEquals(
            expected = HistoryView.CREATIONS,
            actual = selectedView,
        )

        // When
        onRoot().performTouchInput { swipeRight() }

        // Then
        onNodeWithText(getString(Res.string.scans)).assertIsSelected()
    }

    @Test
    fun `an empty view says so and has nothing to clear`() = runComposeUiTest {
        // Given
        var selectedView by mutableStateOf(HistoryView.SCANS)
        setContent {
            historyScreen(
                selectedView = selectedView,
                scanSections = emptyList(),
                creationSections = emptyList(),
            )
        }

        // Then
        onNodeWithText(getString(Res.string.no_scan_yet)).assertExists()
        onNodeWithContentDescription(getString(Res.string.clear_history)).assertDoesNotExist()

        // When
        selectedView = HistoryView.CREATIONS

        // Then
        onNodeWithText(getString(Res.string.no_creation_yet)).assertExists()
    }

    @Test
    fun `the history is cleared once confirmed and only the view shown is`() = runComposeUiTest {
        // Given
        var view by mutableStateOf(HistoryView.SCANS)
        var clearCount = 0
        setContent {
            SCTheme {
                ClearHistoryDialog(
                    view = view,
                    onConfirmClicked = { clearCount++ },
                    onDismissRequest = {},
                )
            }
        }
        onNodeWithText(getString(Res.string.clear_history_question)).assertExists()
        onNodeWithText(getString(Res.string.all_scans_deleted)).assertExists()

        // When
        view = HistoryView.CREATIONS
        onNodeWithText(getString(Res.string.clear)).performClick()

        // Then
        onNodeWithText(getString(Res.string.all_creations_deleted)).assertExists()
        assertEquals(
            expected = 1,
            actual = clearCount,
        )
    }

    @Composable
    private fun historyScreen(
        selectedView: HistoryView,
        scanSections: List<ScanHistorySection> = SCAN_SECTIONS,
        creationSections: List<CreationHistorySection> = CREATION_SECTIONS,
        onViewSelected: (HistoryView) -> Unit = {},
        onScanClicked: (ScanHistoryItem) -> Unit = {},
        onCreationClicked: (CreationHistoryItem) -> Unit = {},
    ) {
        SCTheme {
            HistoryScreen(
                selectedView = selectedView,
                scanSections = scanSections,
                creationSections = creationSections,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onViewSelected = onViewSelected,
                onClearClicked = {},
                onScanClicked = onScanClicked,
                onCreationClicked = onCreationClicked,
            )
        }
    }

    private companion object {
        val LINK = ScanHistoryItem(
            scanId = 2,
            firstCodeId = 2,
            type = CodeType.LINK,
            summary = "https://scanny.app",
            time = "21:48",
            codeCount = 1,
        )
        val SCAN_SECTIONS = listOf(
            ScanHistorySection(
                day = CalendarDay.Today,
                items = listOf(
                    LINK,
                    ScanHistoryItem(
                        scanId = 1,
                        firstCodeId = 1,
                        type = CodeType.TEXT,
                        summary = "First of three",
                        time = "09:05",
                        codeCount = 3,
                    ),
                ),
            ),
        )
        val CREATION = CreationHistoryItem(
            creationId = 1,
            type = CodeType.TEXT,
            summary = "Created for the history",
            time = "10:30",
        )
        val CREATION_SECTIONS = listOf(
            CreationHistorySection(
                day = CalendarDay.Yesterday,
                items = listOf(CREATION),
            ),
        )
    }
}

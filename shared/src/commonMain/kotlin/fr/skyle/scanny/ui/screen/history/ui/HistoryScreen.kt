package fr.skyle.scanny.ui.screen.history.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.segmentedcontrol.SCSegmentedControl
import fr.skyle.scanny.designsystem.core.segmentedcontrol.SCSegmentedControlDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.clear_history
import fr.skyle.scanny.resources.history
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.history.ui.component.CreationHistoryList
import fr.skyle.scanny.ui.screen.history.ui.component.ScanHistoryList
import fr.skyle.scanny.ui.screen.history.ui.mapper.toText
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryView
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistorySection
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    selectedView: HistoryView,
    scanSections: List<ScanHistorySection>?,
    creationSections: List<CreationHistorySection>?,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onViewSelected: (HistoryView) -> Unit,
    onClearClicked: () -> Unit,
    onScanClicked: (ScanHistoryItem) -> Unit,
    onCreationClicked: (CreationHistoryItem) -> Unit,
) {
    // Only the view shown is cleared: nothing to clear when it is empty
    val isShownViewEmpty = when (selectedView) {
        HistoryView.SCANS -> scanSections.isNullOrEmpty()
        HistoryView.CREATIONS -> creationSections.isNullOrEmpty()
    }

    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.history),
                onBackClicked = onBackClicked,
                actions = {
                    if (!isShownViewEmpty) {
                        SCIconButton(
                            modifier = Modifier.wrapContentSize(),
                            icon = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.clear_history),
                            onClick = onClearClicked,
                            style = SCIconButtonDefaults.Style.DARK,
                            size = SCIconButtonDefaults.Size.MEDIUM,
                        )
                    }
                },
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            SCSegmentedControl(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                options = HistoryView.entries,
                selectedOption = selectedView,
                optionText = { view -> stringResource(view.toText()) },
                onOptionSelected = onViewSelected,
                style = SCSegmentedControlDefaults.Style.PRIMARY,
                size = SCSegmentedControlDefaults.Size.MEDIUM,
            )

            // The list goes under the navigation bar, its last row above it
            val listPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())
            when (selectedView) {
                HistoryView.SCANS -> ScanHistoryList(
                    modifier = Modifier.fillMaxSize(),
                    sections = scanSections,
                    contentPadding = listPadding,
                    onScanClicked = onScanClicked,
                )

                HistoryView.CREATIONS -> CreationHistoryList(
                    modifier = Modifier.fillMaxSize(),
                    sections = creationSections,
                    contentPadding = listPadding,
                    onCreationClicked = onCreationClicked,
                )
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewHistoryScreen() {
    SCTheme {
        HistoryScreen(
            selectedView = HistoryView.SCANS,
            scanSections = listOf(
                ScanHistorySection(
                    day = CalendarDay.Today,
                    items = listOf(
                        ScanHistoryItem(
                            scanId = 3,
                            firstCodeId = 3,
                            type = CodeType.LINK,
                            summary = "https://scanny.app",
                            time = "21:48",
                            codeCount = 1,
                        ),
                        ScanHistoryItem(
                            scanId = 2,
                            firstCodeId = 2,
                            type = CodeType.WIFI,
                            summary = "Scanny",
                            time = "09:05",
                            codeCount = 1,
                        ),
                    ),
                ),
                ScanHistorySection(
                    day = CalendarDay.Date(
                        date = LocalDate(
                            year = 2026,
                            month = 11,
                            day = 18,
                        ),
                    ),
                    items = listOf(
                        ScanHistoryItem(
                            scanId = 1,
                            firstCodeId = 1,
                            type = CodeType.CONTACT,
                            summary = "John Doe",
                            time = "18:30",
                            codeCount = 1,
                        ),
                    ),
                ),
            ),
            creationSections = emptyList(),
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onViewSelected = {},
            onClearClicked = {},
            onScanClicked = {},
            onCreationClicked = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewHistoryScreen_EmptyCreations() {
    SCTheme {
        HistoryScreen(
            selectedView = HistoryView.CREATIONS,
            scanSections = emptyList(),
            creationSections = emptyList(),
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onViewSelected = {},
            onClearClicked = {},
            onScanClicked = {},
            onCreationClicked = {},
        )
    }
}

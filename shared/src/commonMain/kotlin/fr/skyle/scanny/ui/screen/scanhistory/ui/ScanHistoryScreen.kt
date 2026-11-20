package fr.skyle.scanny.ui.screen.scanhistory.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
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
import fr.skyle.scanny.ui.screen.scanhistory.ui.component.ScanHistoryDayHeader
import fr.skyle.scanny.ui.screen.scanhistory.ui.component.ScanHistoryEmpty
import fr.skyle.scanny.ui.screen.scanhistory.ui.component.ScanHistoryRow
import fr.skyle.scanny.ui.screen.scanhistory.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.scanhistory.ui.model.ScanHistorySection
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanHistoryScreen(
    sections: List<ScanHistorySection>?,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onScanClicked: (ScanHistoryItem) -> Unit,
) {
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
                    if (!sections.isNullOrEmpty()) {
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
        when {
            // Read in a moment: an empty screen rather than a message that would flash
            sections == null -> Unit

            sections.isEmpty() -> ScanHistoryEmpty(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .wrapContentSize()
                    .padding(horizontal = 32.dp),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                sections.forEach { section ->
                    item(
                        key = section.day.toString(),
                        contentType = "day",
                    ) {
                        ScanHistoryDayHeader(
                            modifier = Modifier.fillMaxWidth(),
                            day = section.day,
                        )
                    }

                    items(
                        items = section.items,
                        key = { item -> item.scanId },
                        contentType = { "scan" },
                    ) { item ->
                        ScanHistoryRow(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                            onClick = { onScanClicked(item) },
                        )
                    }
                }
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanHistoryScreen() {
    SCTheme {
        ScanHistoryScreen(
            sections = listOf(
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
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onClearClicked = {},
            onScanClicked = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanHistoryScreen_Empty() {
    SCTheme {
        ScanHistoryScreen(
            sections = emptyList(),
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onClearClicked = {},
            onScanClicked = {},
        )
    }
}
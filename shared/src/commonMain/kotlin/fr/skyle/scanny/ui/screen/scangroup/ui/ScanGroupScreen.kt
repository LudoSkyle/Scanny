package fr.skyle.scanny.ui.screen.scangroup.ui

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
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.group
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.ic_share
import fr.skyle.scanny.resources.share
import fr.skyle.scanny.ui.core.code.component.CodeRow
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.scangroup.ui.component.ScanGroupHeader
import fr.skyle.scanny.ui.screen.scangroup.ui.model.ScanGroupItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanGroupScreen(
    group: ScanGroupItem?,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onCodeClicked: (codeId: Long) -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.group),
                onBackClicked = onBackClicked,
                actions = {
                    if (group != null) {
                        SCIconButton(
                            modifier = Modifier.wrapContentSize(),
                            icon = painterResource(Res.drawable.ic_share),
                            contentDescription = stringResource(Res.string.share),
                            onClick = onShareClicked,
                            style = SCIconButtonDefaults.Style.DARK,
                            size = SCIconButtonDefaults.Size.MEDIUM,
                        )

                        SCIconButton(
                            modifier = Modifier.wrapContentSize(),
                            icon = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.delete),
                            onClick = onDeleteClicked,
                            style = SCIconButtonDefaults.Style.DARK,
                            size = SCIconButtonDefaults.Size.MEDIUM,
                        )
                    }
                },
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        if (group != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                item(
                    key = "header",
                    contentType = "header",
                ) {
                    ScanGroupHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                top = 8.dp,
                                end = 20.dp,
                                bottom = 16.dp,
                            ),
                        codeCount = group.codes.size,
                        scannedDay = group.scannedDay,
                        scannedTime = group.scannedTime,
                    )
                }

                items(
                    items = group.codes,
                    key = { code -> code.codeId },
                    contentType = { "code" },
                ) { code ->
                    CodeRow(
                        modifier = Modifier.fillMaxWidth(),
                        item = code,
                        onClick = { onCodeClicked(code.codeId) },
                    )
                }
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanGroupScreen() {
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
                    ),
                    CodeRowItem(
                        codeId = 2,
                        type = CodeType.CONTACT,
                        summary = "John Doe",
                    ),
                    CodeRowItem(
                        codeId = 3,
                        type = CodeType.TEXT,
                        summary = "4006381333931",
                    ),
                ),
            ),
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShareClicked = {},
            onDeleteClicked = {},
            onCodeClicked = {},
        )
    }
}
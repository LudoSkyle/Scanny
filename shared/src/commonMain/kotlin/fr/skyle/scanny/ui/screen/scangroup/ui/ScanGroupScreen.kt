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
import fr.skyle.scanny.designsystem.core.card.SCCard
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.codes_of_group
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.group
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.ic_share
import fr.skyle.scanny.resources.share
import fr.skyle.scanny.ui.core.code.component.CodeRow
import fr.skyle.scanny.ui.core.code.component.FavoriteButton
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.list.component.ListSectionHeader
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
    onFavoriteClicked: () -> Unit,
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
                        FavoriteButton(
                            modifier = Modifier.wrapContentSize(),
                            isFavorite = group.isFavorite,
                            onClick = onFavoriteClicked,
                        )

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
            // The group in its card, its codes in another under their title: the two stand apart
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                item(
                    key = "header",
                    contentType = "header",
                ) {
                    SCCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                            ),
                        style = SCCardDefaults.Style.LIGHT,
                    ) {
                        ScanGroupHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            codeCount = group.codes.size,
                            scannedDay = group.scannedDay,
                            scannedTime = group.scannedTime,
                        )
                    }
                }

                // Titled and drawn as the codes of the history
                item(
                    key = "codes",
                    contentType = "codes",
                ) {
                    ListSectionHeader(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.codes_of_group),
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
                        format = BarcodeFormat.QR_CODE,
                    ),
                    CodeRowItem(
                        codeId = 2,
                        type = CodeType.CONTACT,
                        summary = "John Doe",
                        format = BarcodeFormat.QR_CODE,
                    ),
                    CodeRowItem(
                        codeId = 3,
                        type = CodeType.TEXT,
                        summary = "4006381333931",
                        format = BarcodeFormat.QR_CODE,
                    ),
                ),
                isFavorite = false,
            ),
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShareClicked = {},
            onDeleteClicked = {},
            onCodeClicked = {},
            onFavoriteClicked = {},
        )
    }
}
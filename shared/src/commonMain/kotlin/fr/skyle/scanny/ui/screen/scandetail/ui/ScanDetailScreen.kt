package fr.skyle.scanny.ui.screen.scandetail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCCardStructure
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.at_time
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.edit_code
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.ic_edit
import fr.skyle.scanny.resources.ic_qr_code
import fr.skyle.scanny.resources.ic_share
import fr.skyle.scanny.resources.share
import fr.skyle.scanny.resources.show_code
import fr.skyle.scanny.ui.core.code.component.CodeActionButton
import fr.skyle.scanny.ui.core.code.component.CodeFieldRow
import fr.skyle.scanny.ui.core.code.component.CodeHeader
import fr.skyle.scanny.ui.core.code.component.RawContentCard
import fr.skyle.scanny.ui.core.code.mapper.isDrawable
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanDetailScreen(
    code: ScanDetailItem?,
    isDeleting: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onShowCodeClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onCopyClicked: (text: String) -> Unit,
    onActionClicked: (CodeAction) -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                onBackClicked = onBackClicked,
                actions = {
                    if (code != null) {
                        if (code.format?.isDrawable == true) {
                            SCIconButton(
                                modifier = Modifier.wrapContentSize(),
                                icon = painterResource(Res.drawable.ic_qr_code),
                                contentDescription = stringResource(Res.string.show_code),
                                onClick = onShowCodeClicked,
                                style = SCIconButtonDefaults.Style.DARK,
                                size = SCIconButtonDefaults.Size.MEDIUM,
                            )
                        }

                        // Its content in the editor, to be changed and created as a new code
                        SCIconButton(
                            modifier = Modifier.wrapContentSize(),
                            icon = painterResource(Res.drawable.ic_edit),
                            contentDescription = stringResource(Res.string.edit_code),
                            onClick = onEditClicked,
                            style = SCIconButtonDefaults.Style.DARK,
                            size = SCIconButtonDefaults.Size.MEDIUM,
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
                            isEnabled = !isDeleting,
                        )
                    }
                },
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        if (code != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(contentPadding)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
            ) {
                CodeHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    type = code.type,
                    format = code.format,
                )

                Spacer(modifier = Modifier.height(12.dp))

                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    text = stringResource(Res.string.at_time, calendarDayText(day = code.scannedDay), code.scannedTime),
                    style = SCTheme.typography.subtitle2,
                    color = SCTheme.colors.text,
                )

                Spacer(modifier = Modifier.height(24.dp))

                SCCardStructure(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.content),
                    style = SCCardDefaults.Style.LIGHT,
                ) {
                    code.fields.forEach { field ->
                        CodeFieldRow(
                            modifier = Modifier.fillMaxWidth(),
                            label = field.label?.let { label -> stringResource(label.toText()) },
                            value = field.value,
                            onCopyClicked = { onCopyClicked(field.value) },
                        )
                    }
                }

                code.action?.let { action ->
                    Spacer(modifier = Modifier.height(24.dp))

                    CodeActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        action = action,
                        onClick = { onActionClicked(action) },
                    )
                }

                // Last: few need it
                if (code.isRawValueShown) {
                    Spacer(modifier = Modifier.height(24.dp))

                    RawContentCard(
                        modifier = Modifier.fillMaxWidth(),
                        rawValue = code.rawValue,
                        onCopyClicked = { onCopyClicked(code.rawValue) },
                    )
                }
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanDetailScreen() {
    SCTheme {
        ScanDetailScreen(
            code = ScanDetailItem(
                type = CodeType.SMS,
                format = BarcodeFormat.QR_CODE,
                scannedDay = CalendarDay.Today,
                scannedTime = "21:48",
                fields = listOf(
                    CodeField(
                        label = CodeFieldLabel.PHONE_NUMBER,
                        value = "+33600000000",
                    ),
                    CodeField(
                        label = CodeFieldLabel.MESSAGE,
                        value = "See you at 10:30",
                    ),
                ),
                rawValue = "SMSTO:+33600000000:See you at 10:30",
                isRawValueShown = true,
                action = CodeAction.WriteSms(link = "sms:+33600000000?body=See%20you%20at%2010%3A30"),
            ),
            isDeleting = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShowCodeClicked = {},
            onEditClicked = {},
            onShareClicked = {},
            onDeleteClicked = {},
            onCopyClicked = {},
            onActionClicked = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanDetailScreen_Text() {
    SCTheme {
        ScanDetailScreen(
            code = ScanDetailItem(
                type = CodeType.TEXT,
                format = null,
                scannedDay = CalendarDay.Yesterday,
                scannedTime = "09:05",
                fields = listOf(
                    CodeField(
                        label = null,
                        value = "Hello Scanny",
                    ),
                ),
                rawValue = "Hello Scanny",
                isRawValueShown = false,
                action = null,
            ),
            isDeleting = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShowCodeClicked = {},
            onEditClicked = {},
            onShareClicked = {},
            onDeleteClicked = {},
            onCopyClicked = {},
            onActionClicked = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanDetailScreen_Loading() {
    SCTheme {
        ScanDetailScreen(
            code = null,
            isDeleting = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShowCodeClicked = {},
            onEditClicked = {},
            onShareClicked = {},
            onDeleteClicked = {},
            onCopyClicked = {},
            onActionClicked = {},
        )
    }
}
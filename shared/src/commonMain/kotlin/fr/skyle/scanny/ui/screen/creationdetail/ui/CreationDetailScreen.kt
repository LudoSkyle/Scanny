package fr.skyle.scanny.ui.screen.creationdetail.ui

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
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.at_time
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.ic_qr_code
import fr.skyle.scanny.resources.show_code
import fr.skyle.scanny.ui.core.code.component.CodeFieldRow
import fr.skyle.scanny.ui.core.code.component.CodeHeader
import fr.skyle.scanny.ui.core.code.component.RawContentCard
import fr.skyle.scanny.ui.core.code.mapper.isDrawable
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun CreationDetailScreen(
    creation: CreationDetailItem?,
    isDeleting: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onShowCodeClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onCopyClicked: (text: String) -> Unit,
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
                    if (creation != null) {
                        // As in the detail of a scan: the code in a sheet, with the actions on its image
                        if (creation.format?.isDrawable == true) {
                            SCIconButton(
                                modifier = Modifier.wrapContentSize(),
                                icon = painterResource(Res.drawable.ic_qr_code),
                                contentDescription = stringResource(Res.string.show_code),
                                onClick = onShowCodeClicked,
                                style = SCIconButtonDefaults.Style.DARK,
                                size = SCIconButtonDefaults.Size.MEDIUM,
                            )
                        }

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
        if (creation != null) {
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
                    type = creation.type,
                    format = creation.format,
                )

                Spacer(modifier = Modifier.height(12.dp))

                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    text = stringResource(Res.string.at_time, calendarDayText(day = creation.createdDay), creation.createdTime),
                    style = SCTheme.typography.subtitle2,
                    color = SCTheme.colors.text,
                )

                Spacer(modifier = Modifier.height(24.dp))

                SCCardStructure(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.content),
                    style = SCCardDefaults.Style.LIGHT,
                ) {
                    creation.fields.forEach { field ->
                        CodeFieldRow(
                            modifier = Modifier.fillMaxWidth(),
                            label = field.label?.let { label -> stringResource(label.toText()) },
                            value = field.value,
                            onCopyClicked = { onCopyClicked(field.value) },
                        )
                    }
                }

                if (creation.isRawValueShown) {
                    Spacer(modifier = Modifier.height(24.dp))

                    RawContentCard(
                        modifier = Modifier.fillMaxWidth(),
                        rawValue = creation.rawValue,
                        onCopyClicked = { onCopyClicked(creation.rawValue) },
                    )
                }
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCreationDetailScreen() {
    SCTheme {
        CreationDetailScreen(
            creation = CreationDetailItem(
                type = CodeType.LINK,
                format = BarcodeFormat.QR_CODE,
                createdDay = CalendarDay.Today,
                createdTime = "12:00",
                fields = listOf(
                    CodeField(
                        label = null,
                        value = "https://scanny.app",
                    ),
                ),
                rawValue = "https://scanny.app",
                isRawValueShown = false,
                style = CodeStyle(),
            ),
            isDeleting = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShowCodeClicked = {},
            onDeleteClicked = {},
            onCopyClicked = {},
        )
    }
}
package fr.skyle.scanny.ui.screen.creationdetail.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCard
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
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.ic_download
import fr.skyle.scanny.resources.ic_share
import fr.skyle.scanny.resources.save_image
import fr.skyle.scanny.resources.share
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.core.code.component.CodeFieldRow
import fr.skyle.scanny.ui.core.code.component.CodeHeader
import fr.skyle.scanny.ui.core.code.component.CodeImage
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

// Large enough to be scanned from the screen
private const val CODE_WIDTH_FRACTION = 0.7f

@Composable
fun CreationDetailScreen(
    creation: CreationDetailItem?,
    canSaveImage: Boolean,
    isDeleting: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onSaveClicked: () -> Unit,
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
                        // Its image, when this version draws its format
                        if (creation.format?.isDrawable == true) {
                            SCIconButton(
                                modifier = Modifier.wrapContentSize(),
                                icon = painterResource(Res.drawable.ic_share),
                                contentDescription = stringResource(Res.string.share),
                                onClick = onShareClicked,
                                style = SCIconButtonDefaults.Style.DARK,
                                size = SCIconButtonDefaults.Size.MEDIUM,
                            )

                            if (canSaveImage) {
                                SCIconButton(
                                    modifier = Modifier.wrapContentSize(),
                                    icon = painterResource(Res.drawable.ic_download),
                                    contentDescription = stringResource(Res.string.save_image),
                                    onClick = onSaveClicked,
                                    style = SCIconButtonDefaults.Style.DARK,
                                    size = SCIconButtonDefaults.Size.MEDIUM,
                                )
                            }
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

                val format = creation.format
                if (format != null && format.isDrawable) {
                    Spacer(modifier = Modifier.height(24.dp))

                    SCCard(
                        modifier = Modifier.fillMaxWidth(),
                        style = SCCardDefaults.Style.LIGHT,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CodeImage(
                                modifier = Modifier.fillMaxWidth(fraction = CODE_WIDTH_FRACTION),
                                rawValue = creation.rawValue,
                                format = format,
                                contentDescription = stringResource(Res.string.the_code),
                            )
                        }
                    }
                }

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
            ),
            canSaveImage = true,
            isDeleting = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onShareClicked = {},
            onSaveClicked = {},
            onDeleteClicked = {},
            onCopyClicked = {},
        )
    }
}
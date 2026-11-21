package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content_type
import fr.skyle.scanny.resources.create
import fr.skyle.scanny.resources.create_code
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.createcode.ui.component.CodePreviewCard
import fr.skyle.scanny.ui.screen.createcode.ui.component.CodeStyleFields
import fr.skyle.scanny.ui.screen.createcode.ui.component.CreationFormFields
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.format
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateCodeScreen(
    form: CreationForm,
    preview: CodePreview,
    style: CodeStyle,
    isCreating: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onFormChanged: (CreationForm) -> Unit,
    onStyleChanged: (CodeStyle) -> Unit,
    onCreateClicked: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.create_code),
                onBackClicked = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // A touch out of the fields closes the keyboard: the button under it can then be reached
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
                .verticalScroll(state = rememberScrollState())
                .padding(contentPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
        ) {
            SCDropdownField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.content_type),
                placeholder = stringResource(Res.string.content_type),
                options = CreationKind.entries,
                selectedOption = form.kind,
                optionText = { kind -> stringResource(kind.toText()) },
                onOptionSelected = { kind -> onFormChanged(form.copy(kind = kind)) },
                style = SCTextFieldDefaults.Style.LIGHT,
            )

            Spacer(modifier = Modifier.height(16.dp))

            CreationFormFields(
                modifier = Modifier.fillMaxWidth(),
                form = form,
                onFormChanged = onFormChanged,
            )

            Spacer(modifier = Modifier.height(24.dp))

            CodePreviewCard(
                modifier = Modifier.fillMaxWidth(),
                preview = preview,
                style = style,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // A barcode stays dark on white
            if (form.format == BarcodeFormat.QR_CODE) {
                CodeStyleFields(
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    onStyleChanged = onStyleChanged,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            SCButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.create),
                onClick = onCreateClicked,
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
                isEnabled = preview is CodePreview.Code && !isCreating,
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCreateCodeScreen() {
    SCTheme {
        CreateCodeScreen(
            form = CreationForm(
                kind = CreationKind.LINK,
                url = "scanny.app",
            ),
            preview = CodePreview.Code(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
            ),
            style = CodeStyle(),
            isCreating = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onFormChanged = {},
            onStyleChanged = {},
            onCreateClicked = {},
        )
    }
}
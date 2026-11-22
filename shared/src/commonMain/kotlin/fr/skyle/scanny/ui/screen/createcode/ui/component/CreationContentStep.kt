package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content_type
import fr.skyle.scanny.resources.create
import fr.skyle.scanny.resources.next
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.format
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import org.jetbrains.compose.resources.stringResource

/**
 * The first step of the editor: what the code holds, and the code it makes. A QR code goes on to its
 * customization, a barcode is created from here: it stays dark on white.
 */
@Composable
fun CreationContentStep(
    form: CreationForm,
    preview: CodePreview,
    style: CodeStyle,
    isCreating: Boolean,
    contentPadding: PaddingValues,
    onFormChanged: (CreationForm) -> Unit,
    onNextClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isQrCode = form.format == BarcodeFormat.QR_CODE

    Column(
        modifier = modifier
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

        SCButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(if (isQrCode) Res.string.next else Res.string.create),
            onClick = if (isQrCode) onNextClicked else onCreateClicked,
            style = SCButtonDefaults.Style.PRIMARY,
            size = SCButtonDefaults.Size.MEDIUM,
            isEnabled = preview is CodePreview.Code && !isCreating,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationContentStep() {
    SCTheme {
        CreationContentStep(
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
            contentPadding = PaddingValues(),
            onFormChanged = {},
            onNextClicked = {},
            onCreateClicked = {},
        )
    }
}

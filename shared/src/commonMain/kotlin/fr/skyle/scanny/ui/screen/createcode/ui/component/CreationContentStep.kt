package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_family
import fr.skyle.scanny.resources.content_type
import fr.skyle.scanny.resources.format
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.core.code.mapper.toMatrixEncoder
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.format
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationFamily
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import org.jetbrains.compose.resources.stringResource

// The codes in two dimensions other than the QR code, which Scanny draws from their modules
private val matrixFormats = BarcodeFormat.entries.filter { format -> format.toMatrixEncoder() != null }

// The barcodes qrose draws, the formats a barcode can be created in
private val barcodeFormats = BarcodeFormat.entries.filter { format -> format.toBarcodeType() != null }

/**
 * The first step of the editor: the family of the code and what it holds. The code it makes, and the
 * button that goes on, are in the bar fixed under it: a QR code goes on to its customization; another
 * code in two dimensions, or a barcode, is created from here, dark on white.
 */
@Composable
fun CreationContentStep(
    form: CreationForm,
    errorCorrectionChoices: List<ErrorCorrectionChoice>,
    style: CodeStyle,
    contentPadding: PaddingValues,
    onFormChanged: (CreationForm) -> Unit,
    onStyleChanged: (CodeStyle) -> Unit,
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
            // Out of the scroll: a field scrolled to, or focused, stays above the bar under it
            .padding(contentPadding)
            .verticalScroll(state = rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(space = 24.dp),
    ) {
        // The code made, and what it holds: the fields of its content follow, in sections of their own
        CreationSection(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.code_family),
        ) {
            SegmentedChoice(
                modifier = Modifier.fillMaxWidth(),
                options = CreationFamily.entries,
                selectedOption = form.family,
                optionText = { family -> stringResource(family.toText()) },
                onOptionSelected = { family -> onFormChanged(form.copy(family = family)) },
            )

            when (form.family) {
                CreationFamily.QR_CODE -> Unit

                CreationFamily.TWO_DIMENSIONAL -> SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.format),
                    placeholder = stringResource(Res.string.format),
                    options = matrixFormats,
                    selectedOption = form.matrixFormat,
                    optionText = { format -> stringResource(format.toText()) },
                    onOptionSelected = { format -> onFormChanged(form.copy(matrixFormat = format)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )

                CreationFamily.BARCODE -> SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.format),
                    placeholder = stringResource(Res.string.format),
                    options = barcodeFormats,
                    selectedOption = form.barcodeFormat,
                    optionText = { format -> stringResource(format.toText()) },
                    onOptionSelected = { format -> onFormChanged(form.copy(barcodeFormat = format)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
            }

            // A barcode holds a value, of its format
            if (form.family != CreationFamily.BARCODE) {
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
            }
        }

        CreationFormFields(
            modifier = Modifier.fillMaxWidth(),
            form = form,
            onFormChanged = onFormChanged,
        )

        // The correction changes how long a content the code holds: chosen with the content
        if (isQrCode) {
            AdvancedContentOptions(
                modifier = Modifier.fillMaxWidth(),
                errorCorrectionChoices = errorCorrectionChoices,
                selectedErrorCorrection = style.errorCorrection,
                onErrorCorrectionSelected = { errorCorrection -> onStyleChanged(style.copy(errorCorrection = errorCorrection)) },
            )
        }
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
            errorCorrectionChoices = CodeErrorCorrection.entries.map { errorCorrection ->
                ErrorCorrectionChoice(
                    errorCorrection = errorCorrection,
                    blocker = null,
                )
            },
            style = CodeStyle(),
            contentPadding = PaddingValues(),
            onFormChanged = {},
            onStyleChanged = {},
        )
    }
}

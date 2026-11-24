package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.segmentedcontrol.SCSegmentedControl
import fr.skyle.scanny.designsystem.core.segmentedcontrol.SCSegmentedControlDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.save_image
import fr.skyle.scanny.resources.share_image
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeImageFormat
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

// Large enough to be scanned from the screen by another phone
private const val CODE_WIDTH_FRACTION = 0.8f

/**
 * A code drawn again, in its [style], for another phone to scan it from the screen, with the actions
 * on its image, in the format chosen: share it, keep it on the device. A PNG goes anywhere; an SVG
 * image stays sharp at any size, for a print. The messages about the image show in the sheet
 * ([snackbarState]): a message of the screen would show under it.
 */
@Composable
fun CodeImageSheet(
    rawValue: String,
    format: BarcodeFormat,
    style: CodeStyle,
    canSave: Boolean,
    snackbarState: SCSnackbarState,
    onShareClicked: (CodeImageFormat) -> Unit,
    onSaveClicked: (CodeImageFormat) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var imageFormat by rememberSaveable { mutableStateOf(CodeImageFormat.PNG) }

    SCBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        snackbarState = snackbarState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CodeImage(
                modifier = Modifier.fillMaxWidth(fraction = CODE_WIDTH_FRACTION),
                rawValue = rawValue,
                format = format,
                contentDescription = stringResource(Res.string.the_code),
                style = style,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SCSegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = CodeImageFormat.entries,
                selectedOption = imageFormat,
                optionText = { option -> stringResource(option.toText()) },
                onOptionSelected = { option -> imageFormat = option },
                style = SCSegmentedControlDefaults.Style.PRIMARY,
                size = SCSegmentedControlDefaults.Size.MEDIUM,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SCButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.share_image),
                onClick = { onShareClicked(imageFormat) },
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
            )

            if (canSave) {
                Spacer(modifier = Modifier.height(12.dp))

                SCButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.save_image),
                    onClick = { onSaveClicked(imageFormat) },
                    style = SCButtonDefaults.Style.SECONDARY,
                    size = SCButtonDefaults.Size.MEDIUM,
                )
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCodeImageSheet() {
    SCTheme {
        CodeImageSheet(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(
                darkColor = 0xFF3B43BE,
                dotShape = CodeShape.ROUNDED,
            ),
            canSave = true,
            snackbarState = rememberSCSnackbarState(),
            onShareClicked = {},
            onSaveClicked = {},
            onDismissRequest = {},
        )
    }
}

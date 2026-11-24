package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.export_svg
import fr.skyle.scanny.resources.save_image
import fr.skyle.scanny.resources.share_image
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

// Large enough to be scanned from the screen by another phone
private const val CODE_WIDTH_FRACTION = 0.8f

/**
 * A code drawn again, in its [style], for another phone to scan it from the screen, with the actions
 * on its image: share it, keep it in the photos, export it as an SVG image, which stays sharp at any
 * size for a print. The messages about the image show in the sheet
 * ([snackbarState]): a message of the screen would show under it.
 */
@Composable
fun CodeImageSheet(
    rawValue: String,
    format: BarcodeFormat,
    style: CodeStyle,
    canSave: Boolean,
    snackbarState: SCSnackbarState,
    onShareClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onExportSvgClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

            SCButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.share_image),
                onClick = onShareClicked,
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
            )

            if (canSave) {
                Spacer(modifier = Modifier.height(12.dp))

                SCButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.save_image),
                    onClick = onSaveClicked,
                    style = SCButtonDefaults.Style.SECONDARY,
                    size = SCButtonDefaults.Size.MEDIUM,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SCButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.export_svg),
                onClick = onExportSvgClicked,
                style = SCButtonDefaults.Style.SECONDARY,
                size = SCButtonDefaults.Size.MEDIUM,
            )
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
            onExportSvgClicked = {},
            onDismissRequest = {},
        )
    }
}

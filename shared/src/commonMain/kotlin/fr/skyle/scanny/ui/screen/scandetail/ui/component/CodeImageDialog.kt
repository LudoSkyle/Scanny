package fr.skyle.scanny.ui.screen.scandetail.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.dialog.SCContentDialog
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.ok
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.core.code.component.CodeImage
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

/** The scanned code drawn again, for another phone to scan it from the screen. */
@Composable
fun CodeImageDialog(
    rawValue: String,
    format: BarcodeFormat,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCContentDialog(
        modifier = modifier,
        title = stringResource(Res.string.the_code),
        closeText = stringResource(Res.string.ok),
        onDismissRequest = onDismissRequest,
    ) {
        CodeImage(
            modifier = Modifier.fillMaxWidth(),
            rawValue = rawValue,
            format = format,
            contentDescription = stringResource(Res.string.the_code),
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewCodeImageDialog() {
    SCTheme {
        CodeImageDialog(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            onDismissRequest = {},
        )
    }
}
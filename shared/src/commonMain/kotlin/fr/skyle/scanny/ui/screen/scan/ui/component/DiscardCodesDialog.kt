package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.dialog.SCDialog
import fr.skyle.scanny.designsystem.core.dialog.SCDialogDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.cancel
import fr.skyle.scanny.resources.codes_not_saved
import fr.skyle.scanny.resources.discard
import fr.skyle.scanny.resources.discard_codes_question
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

/** Asked when a multiple scan is stopped before its end, with codes found. */
@Composable
fun DiscardCodesDialog(
    onConfirmClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCDialog(
        modifier = modifier,
        title = stringResource(Res.string.discard_codes_question),
        message = stringResource(Res.string.codes_not_saved),
        confirmText = stringResource(Res.string.discard),
        onConfirmClicked = onConfirmClicked,
        dismissText = stringResource(Res.string.cancel),
        onDismissRequest = onDismissRequest,
        style = SCDialogDefaults.Style.DESTRUCTIVE,
    )
}

@AppScreenPreview
@Composable
private fun PreviewDiscardCodesDialog() {
    SCTheme {
        DiscardCodesDialog(
            onConfirmClicked = {},
            onDismissRequest = {},
        )
    }
}
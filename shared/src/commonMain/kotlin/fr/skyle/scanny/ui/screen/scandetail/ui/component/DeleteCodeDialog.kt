package fr.skyle.scanny.ui.screen.scandetail.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.dialog.SCDialog
import fr.skyle.scanny.designsystem.core.dialog.SCDialogDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.cancel
import fr.skyle.scanny.resources.code_removed_from_history
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.delete_code_question
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteCodeDialog(
    onConfirmClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCDialog(
        modifier = modifier,
        title = stringResource(Res.string.delete_code_question),
        message = stringResource(Res.string.code_removed_from_history),
        confirmText = stringResource(Res.string.delete),
        onConfirmClicked = onConfirmClicked,
        dismissText = stringResource(Res.string.cancel),
        onDismissRequest = onDismissRequest,
        style = SCDialogDefaults.Style.DESTRUCTIVE,
    )
}

@AppScreenPreview
@Composable
private fun PreviewDeleteCodeDialog() {
    SCTheme {
        DeleteCodeDialog(
            onConfirmClicked = {},
            onDismissRequest = {},
        )
    }
}
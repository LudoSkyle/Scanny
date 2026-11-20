package fr.skyle.scanny.ui.screen.scanhistory.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.dialog.SCDialog
import fr.skyle.scanny.designsystem.core.dialog.SCDialogDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.all_scans_deleted
import fr.skyle.scanny.resources.cancel
import fr.skyle.scanny.resources.clear
import fr.skyle.scanny.resources.clear_history_question
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClearHistoryDialog(
    onConfirmClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCDialog(
        modifier = modifier,
        title = stringResource(Res.string.clear_history_question),
        message = stringResource(Res.string.all_scans_deleted),
        confirmText = stringResource(Res.string.clear),
        onConfirmClicked = onConfirmClicked,
        dismissText = stringResource(Res.string.cancel),
        onDismissRequest = onDismissRequest,
        style = SCDialogDefaults.Style.DESTRUCTIVE,
    )
}

@AppScreenPreview
@Composable
private fun PreviewClearHistoryDialog() {
    SCTheme {
        ClearHistoryDialog(
            onConfirmClicked = {},
            onDismissRequest = {},
        )
    }
}
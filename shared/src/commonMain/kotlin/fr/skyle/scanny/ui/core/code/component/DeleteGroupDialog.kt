package fr.skyle.scanny.ui.core.code.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.dialog.SCDialog
import fr.skyle.scanny.designsystem.core.dialog.SCDialogDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.cancel
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.delete_group_question
import fr.skyle.scanny.resources.group_codes_removed_from_history
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteGroupDialog(
    onConfirmClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCDialog(
        modifier = modifier,
        title = stringResource(Res.string.delete_group_question),
        message = stringResource(Res.string.group_codes_removed_from_history),
        confirmText = stringResource(Res.string.delete),
        onConfirmClicked = onConfirmClicked,
        dismissText = stringResource(Res.string.cancel),
        onDismissRequest = onDismissRequest,
        style = SCDialogDefaults.Style.DESTRUCTIVE,
    )
}

@AppScreenPreview
@Composable
private fun PreviewDeleteGroupDialog() {
    SCTheme {
        DeleteGroupDialog(
            onConfirmClicked = {},
            onDismissRequest = {},
        )
    }
}
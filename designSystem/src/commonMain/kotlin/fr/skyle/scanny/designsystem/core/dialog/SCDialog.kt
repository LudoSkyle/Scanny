package fr.skyle.scanny.designsystem.core.dialog

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A question asked before an action: a title, a message, a button to confirm and one to cancel. */
@Composable
fun SCDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirmClicked: () -> Unit,
    dismissText: String,
    onDismissRequest: () -> Unit,
    style: SCDialogDefaults.Style,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                modifier = Modifier.wrapContentSize(),
                onClick = onConfirmClicked,
                colors = ButtonDefaults.textButtonColors(contentColor = style.confirmColor),
            ) {
                SCText(
                    modifier = Modifier.wrapContentSize(),
                    text = confirmText,
                    style = SCTheme.typography.button,
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.wrapContentSize(),
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = SCTheme.colors.textPrimary),
            ) {
                SCText(
                    modifier = Modifier.wrapContentSize(),
                    text = dismissText,
                    style = SCTheme.typography.button,
                )
            }
        },
        title = {
            SCText(
                modifier = Modifier.wrapContentSize(),
                text = title,
                style = SCTheme.typography.h3,
            )
        },
        text = {
            SCText(
                modifier = Modifier.wrapContentSize(),
                text = message,
                style = SCTheme.typography.body2,
            )
        },
        shape = SCTheme.shapes.dialog,
        containerColor = SCTheme.colors.backgroundLight,
        titleContentColor = SCTheme.colors.textDark,
        textContentColor = SCTheme.colors.text,
    )
}

private class SCDialogStyleProvider : PreviewParameterProvider<SCDialogDefaults.Style> {
    override val values = SCDialogDefaults.Style.entries.asSequence()
}

@Preview
@Composable
private fun PreviewSCDialog(
    @PreviewParameter(SCDialogStyleProvider::class) style: SCDialogDefaults.Style,
) {
    SCTheme {
        SCDialog(
            title = "Delete this code?",
            message = "It will be removed from the history.",
            confirmText = "Delete",
            onConfirmClicked = {},
            dismissText = "Cancel",
            onDismissRequest = {},
            style = style,
        )
    }
}
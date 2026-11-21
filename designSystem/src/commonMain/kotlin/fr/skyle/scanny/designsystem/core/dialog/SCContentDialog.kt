package fr.skyle.scanny.designsystem.core.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/** Something to look at, under a title, and a single button that closes it. */
@Composable
fun SCContentDialog(
    title: String,
    closeText: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                modifier = Modifier.wrapContentSize(),
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = SCTheme.colors.textPrimary),
            ) {
                SCText(
                    modifier = Modifier.wrapContentSize(),
                    text = closeText,
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
        text = content,
        shape = SCTheme.shapes.dialog,
        containerColor = SCTheme.colors.backgroundLight,
        titleContentColor = SCTheme.colors.textDark,
        textContentColor = SCTheme.colors.text,
    )
}

@Preview
@Composable
private fun PreviewSCContentDialog() {
    SCTheme {
        SCContentDialog(
            title = "The code",
            closeText = "OK",
            onDismissRequest = {},
        ) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = "What the dialog shows",
                style = SCTheme.typography.body2,
            )
        }
    }
}
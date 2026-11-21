package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.ic_content_copy
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A piece of the content, named unless it is the content itself, and the button to copy it. */
@Composable
fun CodeFieldRow(
    label: String?,
    value: String,
    onCopyClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(
            start = 16.dp,
            top = 8.dp,
            end = 4.dp,
            bottom = 8.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (label != null) {
                SCText(
                    modifier = Modifier.fillMaxWidth(),
                    text = label,
                    style = SCTheme.typography.caption,
                    color = SCTheme.colors.text,
                )

                Spacer(modifier = Modifier.height(2.dp))
            }

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = value,
                style = SCTheme.typography.body1,
                color = SCTheme.colors.textDark,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        SCIconButton(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_content_copy),
            contentDescription = stringResource(Res.string.copy),
            onClick = onCopyClicked,
            style = SCIconButtonDefaults.Style.DARK,
            size = SCIconButtonDefaults.Size.MEDIUM,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeFieldRow() {
    SCTheme {
        CodeFieldRow(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.password),
            value = "secret",
            onCopyClicked = {},
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeFieldRow_LongValueWithoutLabel() {
    SCTheme {
        CodeFieldRow(
            modifier = Modifier.fillMaxWidth(),
            label = null,
            value = "A long text read in a code, that goes on several lines to see how the button to copy it stays in place",
            onCopyClicked = {},
        )
    }
}
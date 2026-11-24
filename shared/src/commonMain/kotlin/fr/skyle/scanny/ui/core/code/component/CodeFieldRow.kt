package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.hidden_password
import fr.skyle.scanny.resources.ic_content_copy
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A piece of the content, named unless it is the content itself, and the button to copy it. A
 * [isSecret] value, a password, is hidden until touched, and copied whole.
 */
@Composable
fun CodeFieldRow(
    label: String?,
    value: String,
    onCopyClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isSecret: Boolean = false,
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
        var isShown by rememberSaveable(value) { mutableStateOf(false) }

        CodeFieldText(
            modifier = Modifier.weight(1f),
            label = label,
            value = if (isSecret && !isShown) stringResource(Res.string.hidden_password) else value,
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Next to the button to copy, both in place whatever the value
        if (isSecret) {
            SecretToggleButton(
                modifier = Modifier.wrapContentSize(),
                isShown = isShown,
                onClick = { isShown = !isShown },
            )
        }

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
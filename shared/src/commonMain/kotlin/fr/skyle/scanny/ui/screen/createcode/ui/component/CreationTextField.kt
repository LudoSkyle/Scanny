package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.textfield.SCTextField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.required_field
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/**
 * A field of the content of a code, on one line unless more are asked for. A field the code cannot do
 * without, [isRequired], has an asterisk after its label. A [supportingText] under it helps to fill it.
 */
@Composable
fun CreationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null,
) {
    SCTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = if (isRequired) stringResource(Res.string.required_field, label) else label,
        style = SCTextFieldDefaults.Style.LIGHT,
        minLines = minLines,
        isSingleLine = minLines == 1,
        keyboardType = keyboardType,
        supportingText = supportingText,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationTextField() {
    SCTheme {
        CreationTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            value = "scanny.app",
            onValueChange = {},
            label = stringResource(Res.string.link),
            keyboardType = KeyboardType.Uri,
        )
    }
}
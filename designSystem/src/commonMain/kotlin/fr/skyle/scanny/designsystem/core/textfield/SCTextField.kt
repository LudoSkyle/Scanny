package fr.skyle.scanny.designsystem.core.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A text the user writes, named by its [label]: on one line when [isSingleLine], else on [minLines]
 * lines at least. [keyboardType] opens the keyboard that suits it: a link, an e-mail, a phone number…
 */
@Composable
fun SCTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    style: SCTextFieldDefaults.Style,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    isSingleLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = {
            SCText(
                modifier = Modifier.wrapContentSize(),
                text = label,
                style = SCTheme.typography.body2,
            )
        },
        textStyle = SCTheme.typography.body1,
        minLines = minLines,
        singleLine = isSingleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = SCTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = style.textColor,
            unfocusedTextColor = style.textColor,
            focusedContainerColor = style.containerColor,
            unfocusedContainerColor = style.containerColor,
            focusedBorderColor = style.focusedColor,
            unfocusedBorderColor = style.unfocusedBorderColor,
            focusedLabelColor = style.focusedColor,
            unfocusedLabelColor = style.labelColor,
            cursorColor = style.focusedColor,
        ),
    )
}

private class SCTextFieldStyleProvider : PreviewParameterProvider<SCTextFieldDefaults.Style> {
    override val values = SCTextFieldDefaults.Style.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCTextField(
    @PreviewParameter(SCTextFieldStyleProvider::class) style: SCTextFieldDefaults.Style,
) {
    SCTheme {
        SCTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            value = "A message written on a few words",
            onValueChange = {},
            label = "Message",
            style = style,
            minLines = 3,
        )
    }
}
package fr.skyle.scanny.designsystem.core.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.text.SCTextWithCode
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A text the user writes, named by its [label]: on one line when [isSingleLine], else on [minLines]
 * lines at least. [keyboardType] opens the keyboard that suits it: a link, an e-mail, a phone number…
 * A [supportingText] under it helps to fill it, its parts between backticks as code. While it holds
 * its [prefill] alone, a scheme such as `https://`, the cursor stays after it, wherever it is touched.
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
    supportingText: String? = null,
    prefill: String? = null,
) {
    // The cursor of a text given before any touch, prefilled, at its end, for the user to go on with
    // it: Material puts it at the start. Kept with the composition of the keyboard as they change
    var selection by remember { mutableStateOf(TextRange(value.length)) }
    var composition by remember { mutableStateOf<TextRange?>(null) }
    val textFieldValue = TextFieldValue(
        text = value,
        selection = TextRange(
            start = selection.start.coerceIn(minimumValue = 0, maximumValue = value.length),
            end = selection.end.coerceIn(minimumValue = 0, maximumValue = value.length),
        ),
        composition = composition?.takeIf { range -> range.max <= value.length },
    )

    OutlinedTextField(
        modifier = modifier,
        value = textFieldValue,
        onValueChange = { changedValue ->
            // Typed after the prefill, never inside it
            selection = if (changedValue.text == prefill) TextRange(changedValue.text.length) else changedValue.selection
            composition = changedValue.composition
            if (changedValue.text != value) {
                onValueChange(changedValue.text)
            }
        },
        label = {
            SCText(
                modifier = Modifier.wrapContentSize(),
                text = label,
                style = SCTheme.typography.body2,
            )
        },
        supportingText = supportingText?.let { text ->
            {
                SCTextWithCode(
                    modifier = Modifier.wrapContentSize(),
                    text = text,
                    style = SCTheme.typography.subtitle2,
                )
            }
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
            focusedSupportingTextColor = style.labelColor,
            unfocusedSupportingTextColor = style.labelColor,
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
package fr.skyle.scanny.designsystem.core.dropdownfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A choice among [options], in a menu that unfolds under the field. The field shows the
 * [selectedOption], or [placeholder] while none is chosen; it looks like an [SCTextField][fr.skyle.scanny.designsystem.core.textfield.SCTextField]
 * and takes its styles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SCDropdownField(
    label: String,
    placeholder: String,
    options: List<T>,
    selectedOption: T?,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    style: SCTextFieldDefaults.Style,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = { expanded ->
            isExpanded = expanded
        },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            value = selectedOption?.let { option -> optionText(option) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = {
                SCText(
                    modifier = Modifier.wrapContentSize(),
                    text = label,
                    style = SCTheme.typography.body2,
                )
            },
            placeholder = {
                SCText(
                    modifier = Modifier.wrapContentSize(),
                    text = placeholder,
                    style = SCTheme.typography.body1,
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            textStyle = SCTheme.typography.body1,
            singleLine = true,
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
                focusedPlaceholderColor = style.labelColor,
                unfocusedPlaceholderColor = style.labelColor,
                focusedTrailingIconColor = style.focusedColor,
                unfocusedTrailingIconColor = style.labelColor,
            ),
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
            },
            shape = SCTheme.shapes.medium,
            containerColor = style.containerColor,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        SCText(
                            modifier = Modifier.wrapContentSize(),
                            text = optionText(option),
                            style = SCTheme.typography.body1,
                            color = style.textColor,
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        isExpanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCDropdownField() {
    SCTheme {
        SCDropdownField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "Subject",
            placeholder = "Choose a subject",
            options = listOf(
                "The scan",
                "Something else",
            ),
            selectedOption = "The scan",
            optionText = { option -> option },
            onOptionSelected = {},
            style = SCTextFieldDefaults.Style.LIGHT,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCDropdownField_NothingChosen() {
    SCTheme {
        SCDropdownField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "Subject",
            placeholder = "Choose a subject",
            options = listOf(
                "The scan",
                "Something else",
            ),
            selectedOption = null,
            optionText = { option -> option },
            onOptionSelected = {},
            style = SCTextFieldDefaults.Style.LIGHT,
        )
    }
}
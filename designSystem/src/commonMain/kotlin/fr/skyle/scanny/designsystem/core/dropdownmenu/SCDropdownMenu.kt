package fr.skyle.scanny.designsystem.core.dropdownmenu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A menu of [options] that unfolds under the element it is placed in, moved by [offset] from its
 * start, the [selectedOption] in the color of a choice. It closes once an option is chosen.
 */
@Composable
fun <T> SCDropdownMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<T>,
    selectedOption: T?,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    style: SCDropdownMenuDefaults.Style,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        offset = offset,
        onDismissRequest = onDismissRequest,
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
                        color = if (option == selectedOption) style.selectedTextColor else style.textColor,
                    )
                },
                onClick = {
                    onDismissRequest()
                    onOptionSelected(option)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCDropdownMenu() {
    SCTheme {
        Box(modifier = Modifier.padding(all = 16.dp)) {
            SCDropdownMenu(
                isExpanded = true,
                onDismissRequest = {},
                options = listOf(
                    "7 days",
                    "30 days",
                    "Forever",
                ),
                selectedOption = "30 days",
                optionText = { option -> option },
                onOptionSelected = {},
                style = SCDropdownMenuDefaults.Style.LIGHT,
            )
        }
    }
}
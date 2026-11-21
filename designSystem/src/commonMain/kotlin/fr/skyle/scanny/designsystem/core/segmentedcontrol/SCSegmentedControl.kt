package fr.skyle.scanny.designsystem.core.segmentedcontrol

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A choice among a few [options], side by side in a pill, each as wide as the others: the chosen one
 * filled. Each option reads as a tab.
 */
@Composable
fun <T> SCSegmentedControl(
    options: List<T>,
    selectedOption: T,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    style: SCSegmentedControlDefaults.Style,
    size: SCSegmentedControlDefaults.Size,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(size.height)
            .clip(CircleShape)
            .background(color = style.containerColor)
            .padding(all = size.padding)
            .selectableGroup(),
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val backgroundColor by animateColorAsState(if (isSelected) style.selectedColor else style.containerColor)
            val textColor by animateColorAsState(if (isSelected) style.selectedTextColor else style.textColor)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color = backgroundColor)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onOptionSelected(option) },
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                SCText(
                    text = optionText(option),
                    style = size.textStyle,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private class SCSegmentedControlVariantProvider :
    PreviewParameterProvider<Pair<SCSegmentedControlDefaults.Style, SCSegmentedControlDefaults.Size>> {
    override val values =
        SCSegmentedControlDefaults.Style.entries.flatMap { style ->
            SCSegmentedControlDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCSegmentedControl(
    @PreviewParameter(SCSegmentedControlVariantProvider::class)
    variant: Pair<SCSegmentedControlDefaults.Style, SCSegmentedControlDefaults.Size>,
) {
    SCTheme {
        SCSegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            options = listOf("Scans", "Creations"),
            selectedOption = "Scans",
            optionText = { option -> option },
            onOptionSelected = {},
            style = variant.first,
            size = variant.second,
        )
    }
}

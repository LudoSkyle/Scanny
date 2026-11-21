package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.other_color
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/**
 * A color to choose among [options], as discs going to the next line on a narrow screen, or any other
 * color: the last disc, [customColor] when it is the one chosen.
 */
@Composable
fun <T> ColorChoiceRow(
    label: String,
    options: List<T>,
    selectedOption: T?,
    optionColor: (T) -> Color,
    optionName: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    customColor: Color?,
    onCustomColorClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = label,
            style = SCTheme.typography.body2,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { option ->
                ColorSwatch(
                    color = optionColor(option),
                    name = optionName(option),
                    isSelected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                )
            }

            CustomColorSwatch(
                color = customColor,
                name = stringResource(Res.string.other_color),
                onClick = onCustomColorClicked,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewColorChoiceRow() {
    SCTheme {
        ColorChoiceRow(
            modifier = Modifier.fillMaxWidth(),
            label = "Color of the dots",
            options = listOf(Color.Black, Color(0xFF3B43BE), Color(0xFF1B5E20)),
            selectedOption = Color.Black,
            optionColor = { color -> color },
            optionName = { color -> color.toString() },
            onOptionSelected = {},
            customColor = null,
            onCustomColorClicked = {},
        )
    }
}

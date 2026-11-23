package fr.skyle.scanny.designsystem.core.radiobutton

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A radio button. Without [onClick], it only shows its state: the row that holds it is then
 * `selectable`, for the whole row to choose it and be read as one radio button.
 */
@Composable
fun SCRadioButton(
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    style: SCRadioButtonDefaults.Style,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    RadioButton(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        enabled = isEnabled,
        colors = RadioButtonDefaults.colors(
            selectedColor = style.selectedColor,
            unselectedColor = style.unselectedColor,
            disabledSelectedColor = style.disabledColor,
            disabledUnselectedColor = style.disabledColor,
        ),
    )
}

private class SCRadioButtonStyleProvider : PreviewParameterProvider<SCRadioButtonDefaults.Style> {
    override val values = SCRadioButtonDefaults.Style.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCRadioButton(
    @PreviewParameter(SCRadioButtonStyleProvider::class) style: SCRadioButtonDefaults.Style,
) {
    SCTheme {
        SCRadioButton(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = 16.dp),
            isSelected = true,
            onClick = {},
            style = style,
        )
    }
}

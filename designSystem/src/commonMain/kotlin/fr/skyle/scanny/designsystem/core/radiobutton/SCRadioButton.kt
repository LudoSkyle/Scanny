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
 * A choice among others. Without [onClick], it only shows whether it is chosen: the row that holds it is
 * then `selectable`, for the whole row to choose it and be read as one choice.
 */
@Composable
fun SCRadioButton(
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    style: SCRadioButtonDefaults.Style,
    modifier: Modifier = Modifier,
) {
    RadioButton(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(
            selectedColor = style.selectedColor,
            unselectedColor = style.unselectedColor,
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
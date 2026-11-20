package fr.skyle.scanny.designsystem.core.switch

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

/**
 * A switch. Without [onCheckedChange], it only shows its state: the row that holds it is then
 * `toggleable`, for the whole row to switch it and be read as one switch.
 */
@Composable
fun SCSwitch(
    isChecked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    style: SCSwitchDefaults.Style,
    modifier: Modifier = Modifier,
) {
    Switch(
        modifier = modifier,
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = style.checkedThumbColor,
            checkedTrackColor = style.checkedTrackColor,
            uncheckedThumbColor = style.uncheckedThumbColor,
            uncheckedTrackColor = style.uncheckedTrackColor,
            uncheckedBorderColor = style.uncheckedThumbColor,
        ),
    )
}

private class SCSwitchStyleProvider : PreviewParameterProvider<SCSwitchDefaults.Style> {
    override val values = SCSwitchDefaults.Style.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCSwitch(
    @PreviewParameter(SCSwitchStyleProvider::class) style: SCSwitchDefaults.Style,
) {
    SCTheme {
        SCSwitch(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = 16.dp),
            isChecked = true,
            onCheckedChange = {},
            style = style,
        )
    }
}
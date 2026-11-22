package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.switch.SCSwitch
import fr.skyle.scanny.designsystem.core.switch.SCSwitchDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.hidden_network
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/** An option of the content, on or off, named by its [label]: switched by a touch anywhere on the row. */
@Composable
fun CreationSwitchRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .toggleable(
                value = isChecked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(
                horizontal = 4.dp,
                vertical = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCText(
            modifier = Modifier.weight(1f),
            text = label,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )

        Spacer(modifier = Modifier.width(16.dp))

        SCSwitch(
            modifier = Modifier.wrapContentSize(),
            isChecked = isChecked,
            onCheckedChange = null,
            style = SCSwitchDefaults.Style.PRIMARY,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationSwitchRow() {
    SCTheme {
        CreationSwitchRow(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.hidden_network),
            isChecked = true,
            onCheckedChange = {},
        )
    }
}
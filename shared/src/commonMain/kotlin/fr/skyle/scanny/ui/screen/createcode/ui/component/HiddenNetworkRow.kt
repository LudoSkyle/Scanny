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

/** Whether the network created does not show its name: switched by a touch anywhere on the row. */
@Composable
fun HiddenNetworkRow(
    isHidden: Boolean,
    onHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .toggleable(
                value = isHidden,
                role = Role.Switch,
                onValueChange = onHiddenChange,
            )
            .padding(
                horizontal = 4.dp,
                vertical = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCText(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.hidden_network),
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )

        Spacer(modifier = Modifier.width(16.dp))

        SCSwitch(
            modifier = Modifier.wrapContentSize(),
            isChecked = isHidden,
            onCheckedChange = null,
            style = SCSwitchDefaults.Style.PRIMARY,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewHiddenNetworkRow() {
    SCTheme {
        HiddenNetworkRow(
            modifier = Modifier.fillMaxWidth(),
            isHidden = true,
            onHiddenChange = {},
        )
    }
}
package fr.skyle.scanny.ui.screen.settings.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.switch.SCSwitch
import fr.skyle.scanny.designsystem.core.switch.SCSwitchDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.ic_vibration
import fr.skyle.scanny.resources.vibrate_after_scan
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A setting switched on or off by a touch anywhere on its row. */
@Composable
fun SettingsSwitchRow(
    icon: Painter,
    title: String,
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
            .heightIn(min = 56.dp)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCIconBadge(
            modifier = Modifier.wrapContentSize(),
            icon = icon,
            contentDescription = null,
            style = SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.MEDIUM,
        )

        Spacer(modifier = Modifier.width(16.dp))

        SCText(
            modifier = Modifier.weight(1f),
            text = title,
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
private fun PreviewSettingsSwitchRow() {
    SCTheme {
        SettingsSwitchRow(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(Res.drawable.ic_vibration),
            title = stringResource(Res.string.vibrate_after_scan),
            isChecked = true,
            onCheckedChange = {},
        )
    }
}
package fr.skyle.scanny.ui.screen.settings.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.give_feedback
import fr.skyle.scanny.resources.ic_chat
import fr.skyle.scanny.resources.ic_chevron_right
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A row that does something when touched. Its [trailingIcon] says where it leads: another screen (a
 * chevron) or out of the app (an arrow out); none when it acts in place, such as asking to clear the
 * history.
 */
@Composable
fun SettingsActionRow(
    icon: Painter,
    title: String,
    trailingIcon: Painter?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
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

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(16.dp))

            // The title already names the row
            Image(
                modifier = Modifier.size(24.dp),
                painter = trailingIcon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = SCTheme.colors.text),
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewSettingsActionRow() {
    SCTheme {
        SettingsActionRow(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(Res.drawable.ic_chat),
            title = stringResource(Res.string.give_feedback),
            trailingIcon = painterResource(Res.drawable.ic_chevron_right),
            onClick = {},
        )
    }
}
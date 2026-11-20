package fr.skyle.scanny.ui.core.link.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.about
import fr.skyle.scanny.resources.ic_chevron_right
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A row that leads somewhere: another screen (a chevron) or out of the app (an arrow out). */
@Composable
fun LinkRow(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCText(
            modifier = Modifier.weight(1f),
            text = title,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Image(
            modifier = Modifier.size(24.dp),
            painter = icon,
            contentDescription = title,
            colorFilter = ColorFilter.tint(color = SCTheme.colors.text),
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewLinkRow() {
    SCTheme {
        LinkRow(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.about),
            icon = painterResource(Res.drawable.ic_chevron_right),
            onClick = {},
        )
    }
}
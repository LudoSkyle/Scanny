package fr.skyle.scanny.designsystem.core.iconbadge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.ic_arrow_back
import fr.skyle.scanny.designsystem.theme.SCTheme
import org.jetbrains.compose.resources.painterResource

/** An icon in a round badge, not clickable: what a thing is, such as the type of a content. */
@Composable
fun SCIconBadge(
    icon: Painter,
    contentDescription: String,
    style: SCIconBadgeDefaults.Style,
    size: SCIconBadgeDefaults.Size,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.badgeSize)
            .background(
                color = style.containerColor,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(size.iconSize),
            painter = icon,
            contentDescription = contentDescription,
            tint = style.contentColor,
        )
    }
}

private class SCIconBadgeVariantProvider : PreviewParameterProvider<Pair<SCIconBadgeDefaults.Style, SCIconBadgeDefaults.Size>> {
    override val values =
        SCIconBadgeDefaults.Style.entries.flatMap { style ->
            SCIconBadgeDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCIconBadge(
    @PreviewParameter(SCIconBadgeVariantProvider::class) variant: Pair<SCIconBadgeDefaults.Style, SCIconBadgeDefaults.Size>,
) {
    SCTheme {
        SCIconBadge(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = 16.dp),
            icon = painterResource(Res.drawable.ic_arrow_back),
            contentDescription = "Back",
            style = variant.first,
            size = variant.second,
        )
    }
}
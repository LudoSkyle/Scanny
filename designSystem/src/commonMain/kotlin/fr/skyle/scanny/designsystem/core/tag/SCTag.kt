package fr.skyle.scanny.designsystem.core.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A short label in a pill, which sorts what it goes with: a type, a kind… Not clickable. */
@Composable
fun SCTag(
    text: String,
    style: SCTagDefaults.Style,
    size: SCTagDefaults.Size,
    modifier: Modifier = Modifier,
) {
    SCText(
        modifier = modifier
            .clip(CircleShape)
            .background(color = style.containerColor)
            .padding(
                horizontal = size.horizontalPadding,
                vertical = size.verticalPadding,
            ),
        text = text,
        style = size.textStyle,
        color = style.contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private class SCTagVariantProvider : PreviewParameterProvider<Pair<SCTagDefaults.Style, SCTagDefaults.Size>> {
    override val values =
        SCTagDefaults.Style.entries.flatMap { style ->
            SCTagDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCTag(
    @PreviewParameter(SCTagVariantProvider::class) variant: Pair<SCTagDefaults.Style, SCTagDefaults.Size>,
) {
    SCTheme {
        SCTag(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = 16.dp),
            text = "Link",
            style = variant.first,
            size = variant.second,
        )
    }
}

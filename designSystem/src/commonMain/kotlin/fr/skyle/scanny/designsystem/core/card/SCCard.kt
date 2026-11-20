package fr.skyle.scanny.designsystem.core.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A block of content set apart from the background of the screen. */
@Composable
fun SCCard(
    style: SCCardDefaults.Style,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides style.contentColor) {
        Column(
            modifier = modifier
                .clip(SCTheme.shapes.medium)
                .background(color = style.containerColor),
            content = content,
        )
    }
}

private class SCCardStyleProvider : PreviewParameterProvider<SCCardDefaults.Style> {
    override val values = SCCardDefaults.Style.entries.asSequence()
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF5F6FC,
)
@Composable
private fun PreviewSCCard(
    @PreviewParameter(SCCardStyleProvider::class) style: SCCardDefaults.Style,
) {
    SCTheme {
        SCCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            style = style,
        ) {
            SCText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                text = "Content",
                style = SCTheme.typography.body1,
            )
        }
    }
}
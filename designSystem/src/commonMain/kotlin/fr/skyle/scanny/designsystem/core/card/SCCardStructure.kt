package fr.skyle.scanny.designsystem.core.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A titled block of a screen: its title above, its content in an [SCCard]. */
@Composable
fun SCCardStructure(
    title: String,
    style: SCCardDefaults.Style,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
                .padding(horizontal = 4.dp),
            text = title,
            style = SCTheme.typography.subtitle1,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SCCard(
            modifier = Modifier.fillMaxWidth(),
            style = style,
            content = content,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF5F6FC,
)
@Composable
private fun PreviewSCCardStructure() {
    SCTheme {
        SCCardStructure(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            title = "Title",
            style = SCCardDefaults.Style.LIGHT,
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
package fr.skyle.scanny.ui.screen.scandetail.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCard
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/** A titled block of the detail, such as the content or the raw content. */
@Composable
fun ScanDetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = title,
            style = SCTheme.typography.subtitle1,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SCCard(
            modifier = Modifier.fillMaxWidth(),
            style = SCCardDefaults.Style.LIGHT,
            content = content,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewScanDetailSection() {
    SCTheme {
        ScanDetailSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            title = stringResource(Res.string.content),
        ) {
            ScanDetailFieldRow(
                modifier = Modifier.fillMaxWidth(),
                label = null,
                value = "https://scanny.app",
                onCopyClicked = {},
            )
        }
    }
}
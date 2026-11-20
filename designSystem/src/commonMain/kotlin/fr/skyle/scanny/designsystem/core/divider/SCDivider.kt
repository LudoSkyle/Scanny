package fr.skyle.scanny.designsystem.core.divider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A thin line between two rows. */
@Composable
fun SCDivider(
    style: SCDividerDefaults.Style,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = style.color,
    )
}

private class SCDividerStyleProvider : PreviewParameterProvider<SCDividerDefaults.Style> {
    override val values = SCDividerDefaults.Style.entries.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCDivider(
    @PreviewParameter(SCDividerStyleProvider::class) style: SCDividerDefaults.Style,
) {
    SCTheme {
        SCDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            style = style,
        )
    }
}
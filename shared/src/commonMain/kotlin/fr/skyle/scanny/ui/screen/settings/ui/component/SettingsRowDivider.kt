package fr.skyle.scanny.ui.screen.settings.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.divider.SCDivider
import fr.skyle.scanny.designsystem.core.divider.SCDividerDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground

/** The line between two rows of settings, starting under their titles, past the badges. */
@Composable
fun SettingsRowDivider(
    modifier: Modifier = Modifier,
) {
    SCDivider(
        modifier = modifier.padding(start = 72.dp),
        style = SCDividerDefaults.Style.LIGHT,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewSettingsRowDivider() {
    SCTheme {
        SettingsRowDivider(modifier = Modifier.fillMaxWidth())
    }
}
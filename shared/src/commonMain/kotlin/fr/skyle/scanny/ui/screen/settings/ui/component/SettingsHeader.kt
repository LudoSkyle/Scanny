package fr.skyle.scanny.ui.screen.settings.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCard
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.app_description
import fr.skyle.scanny.resources.app_name
import fr.skyle.scanny.resources.scanny_logo
import fr.skyle.scanny.resources.version_and_build
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** What the app is: its logo, its name, its version and what it does. */
@Composable
fun SettingsHeader(
    versionName: String,
    build: String,
    modifier: Modifier = Modifier,
) {
    SCCard(
        modifier = modifier,
        style = SCCardDefaults.Style.LIGHT,
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // The name beside it already says it
                Image(
                    modifier = Modifier.size(56.dp),
                    painter = painterResource(Res.drawable.scanny_logo),
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    SCText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.app_name),
                        style = SCTheme.typography.h3,
                        color = SCTheme.colors.textDark,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    SCText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.version_and_build, versionName, build),
                        style = SCTheme.typography.caption,
                        color = SCTheme.colors.text,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.app_description),
                style = SCTheme.typography.body2,
                color = SCTheme.colors.text,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewSettingsHeader() {
    SCTheme {
        SettingsHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            versionName = "2.0",
            build = "20",
        )
    }
}
package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_logo
import fr.skyle.scanny.resources.change_logo
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.logo
import fr.skyle.scanny.resources.remove_logo
import fr.skyle.scanny.resources.the_logo
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The image put at the center of a QR code, picked in the photos: added, changed or removed. */
@Composable
fun LogoRow(
    logo: ByteArray?,
    blocker: LogoBlocker?,
    onAddLogoClicked: () -> Unit,
    onRemoveLogoClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = stringResource(Res.string.logo),
            style = SCTheme.typography.body2,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Already reduced when picked: read at once
            val image = remember(logo) { logo?.let { png -> runCatching { png.decodeToImageBitmap() }.getOrNull() } }
            if (image != null) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(SCTheme.shapes.medium),
                    bitmap = image,
                    contentDescription = stringResource(Res.string.the_logo),
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            SCButton(
                modifier = Modifier.weight(1f),
                text = stringResource(if (logo != null) Res.string.change_logo else Res.string.add_logo),
                onClick = onAddLogoClicked,
                style = SCButtonDefaults.Style.SECONDARY,
                size = SCButtonDefaults.Size.MEDIUM,
                isEnabled = blocker == null,
            )

            if (logo != null) {
                Spacer(modifier = Modifier.width(8.dp))

                SCIconButton(
                    modifier = Modifier.wrapContentSize(),
                    icon = painterResource(Res.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.remove_logo),
                    onClick = onRemoveLogoClicked,
                    style = SCIconButtonDefaults.Style.DARK,
                    size = SCIconButtonDefaults.Size.MEDIUM,
                )
            }
        }

        blocker?.let { reason ->
            Spacer(modifier = Modifier.height(8.dp))

            SCText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                text = stringResource(reason.toText()),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewLogoRow() {
    SCTheme {
        LogoRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            logo = null,
            blocker = LogoBlocker.CORRECTION_TOO_LOW,
            onAddLogoClicked = {},
            onRemoveLogoClicked = {},
        )
    }
}

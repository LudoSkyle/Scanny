package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.allow_camera
import fr.skyle.scanny.resources.camera_needed
import fr.skyle.scanny.resources.ic_no_photography
import fr.skyle.scanny.ui.core.preview.AppPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Why the camera does not show, and the action that fixes it when there is one. */
@Composable
fun CameraMessage(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClicked: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(Res.drawable.ic_no_photography),
            contentDescription = message,
            colorFilter = ColorFilter.tint(color = SCTheme.colors.textLight),
        )

        Spacer(modifier = Modifier.height(16.dp))

        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = message,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textLight,
            textAlign = TextAlign.Center,
        )

        if (actionText != null) {
            Spacer(modifier = Modifier.height(24.dp))

            SCButton(
                modifier = Modifier.wrapContentWidth(),
                text = actionText,
                onClick = onActionClicked,
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
            )
        }
    }
}

@AppPreview
@Composable
private fun PreviewCameraMessage() {
    SCTheme {
        CameraMessage(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = SCTheme.colors.backgroundBlack)
                .padding(all = 32.dp),
            message = stringResource(Res.string.camera_needed),
            actionText = stringResource(Res.string.allow_camera),
        )
    }
}
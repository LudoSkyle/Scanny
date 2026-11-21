package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.create_code
import fr.skyle.scanny.resources.history
import fr.skyle.scanny.resources.ic_add
import fr.skyle.scanny.resources.ic_history
import fr.skyle.scanny.resources.ic_settings
import fr.skyle.scanny.resources.settings
import fr.skyle.scanny.ui.core.preview.AppPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The ways out of the scan, over the camera: the scan is the first screen and leads to every other. */
@Composable
fun ScanTopActions(
    onCreateClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        // The same gap between every action, whichever are shown
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
    ) {
        SCIconButton(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.create_code),
            onClick = onCreateClicked,
            style = SCIconButtonDefaults.Style.OVERLAY,
            size = SCIconButtonDefaults.Size.MEDIUM,
        )

        SCIconButton(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_history),
            contentDescription = stringResource(Res.string.history),
            onClick = onHistoryClicked,
            style = SCIconButtonDefaults.Style.OVERLAY,
            size = SCIconButtonDefaults.Size.MEDIUM,
        )

        SCIconButton(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_settings),
            contentDescription = stringResource(Res.string.settings),
            onClick = onSettingsClicked,
            style = SCIconButtonDefaults.Style.OVERLAY,
            size = SCIconButtonDefaults.Size.MEDIUM,
        )
    }
}

@AppPreview
@Composable
private fun PreviewScanTopActions() {
    SCTheme {
        ScanTopActions(
            modifier = Modifier
                .wrapContentSize()
                .background(color = SCTheme.colors.primary)
                .padding(all = 16.dp),
            onCreateClicked = {},
            onHistoryClicked = {},
            onSettingsClicked = {},
        )
    }
}
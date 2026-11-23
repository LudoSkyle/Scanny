package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.ic_flashlight_off
import fr.skyle.scanny.resources.ic_flashlight_on
import fr.skyle.scanny.resources.ic_photo_library
import fr.skyle.scanny.resources.ic_stacks
import fr.skyle.scanny.resources.multiple_scan
import fr.skyle.scanny.resources.scan_image
import fr.skyle.scanny.resources.turn_off_torch
import fr.skyle.scanny.resources.turn_on_torch
import fr.skyle.scanny.ui.core.preview.AppPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanControls(
    hasTorch: Boolean,
    isTorchEnabled: Boolean,
    isMultipleScanActive: Boolean,
    onScanImageClicked: () -> Unit,
    onMultipleScanClicked: () -> Unit,
    onTorchClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        // The same gap between every control, whichever are shown
        horizontalArrangement = Arrangement.spacedBy(space = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCIconButton(
            icon = painterResource(Res.drawable.ic_photo_library),
            contentDescription = stringResource(Res.string.scan_image),
            onClick = onScanImageClicked,
            style = SCIconButtonDefaults.Style.OVERLAY,
            size = SCIconButtonDefaults.Size.LARGE,
        )

        SCIconButton(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_stacks),
            contentDescription = stringResource(Res.string.multiple_scan),
            onClick = onMultipleScanClicked,
            style = if (isMultipleScanActive) SCIconButtonDefaults.Style.PRIMARY else SCIconButtonDefaults.Style.OVERLAY,
            size = SCIconButtonDefaults.Size.LARGE,
        )

        if (hasTorch) {
            SCIconButton(
                icon = painterResource(if (isTorchEnabled) Res.drawable.ic_flashlight_on else Res.drawable.ic_flashlight_off),
                contentDescription = stringResource(if (isTorchEnabled) Res.string.turn_off_torch else Res.string.turn_on_torch),
                onClick = onTorchClicked,
                style = SCIconButtonDefaults.Style.OVERLAY,
                size = SCIconButtonDefaults.Size.LARGE,
            )
        }
    }
}

@AppPreview
@Composable
private fun PreviewScanControls() {
    SCTheme {
        ScanControls(
            modifier = Modifier
                .wrapContentSize()
                .background(color = SCTheme.colors.primary)
                .padding(all = 16.dp),
            hasTorch = true,
            isTorchEnabled = false,
            isMultipleScanActive = true,
            onScanImageClicked = {},
            onMultipleScanClicked = {},
            onTorchClicked = {},
        )
    }
}
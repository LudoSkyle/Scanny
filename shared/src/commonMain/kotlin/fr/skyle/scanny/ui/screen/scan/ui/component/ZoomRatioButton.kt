package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.reset_zoom
import fr.skyle.scanny.resources.zoom_ratio
import fr.skyle.scanny.ui.core.preview.AppPreview
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * The zoom of the camera, to the tenth and with the decimal separator of the language: « 2,5× ».
 * Touched, it goes back to 1×.
 */
@Composable
fun ZoomRatioButton(
    zoomRatio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tenths = (zoomRatio * 10).roundToInt()

    Box(
        modifier = modifier
            .clip(shape = CircleShape)
            // As the other buttons over the camera
            .background(color = SCTheme.colors.backgroundBlack.copy(alpha = 0.45f))
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(Res.string.reset_zoom),
                onClick = onClick,
            )
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        SCText(
            modifier = Modifier.wrapContentSize(),
            text = stringResource(Res.string.zoom_ratio, tenths / 10, tenths % 10),
            style = SCTheme.typography.button,
            color = SCTheme.colors.textLight,
        )
    }
}

@AppPreview
@Composable
private fun PreviewZoomRatioButton() {
    SCTheme {
        ZoomRatioButton(
            modifier = Modifier
                .wrapContentSize()
                .background(color = SCTheme.colors.primary)
                .padding(all = 16.dp),
            zoomRatio = 2.5f,
            onClick = {},
        )
    }
}

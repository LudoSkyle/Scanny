package fr.skyle.scanny.designsystem.core.iconbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.ic_arrow_back
import fr.skyle.scanny.designsystem.theme.SCTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun SCIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    style: SCIconButtonDefaults.Style,
    size: SCIconButtonDefaults.Size,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = isEnabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = style.contentColor,
            disabledContentColor = SCTheme.colors.textDisabled,
        ),
    ) {
        Icon(
            modifier = Modifier.size(size.iconSize),
            painter = icon,
            contentDescription = contentDescription,
        )
    }
}

private class SCIconButtonVariantProvider : PreviewParameterProvider<Pair<SCIconButtonDefaults.Style, SCIconButtonDefaults.Size>> {
    override val values =
        SCIconButtonDefaults.Style.entries.flatMap { style ->
            SCIconButtonDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview
@Composable
private fun PreviewSCIconButton(
    @PreviewParameter(SCIconButtonVariantProvider::class) variant: Pair<SCIconButtonDefaults.Style, SCIconButtonDefaults.Size>,
) {
    SCTheme {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = when (variant.first) {
                        SCIconButtonDefaults.Style.DARK -> SCTheme.colors.background
                        SCIconButtonDefaults.Style.LIGHT -> SCTheme.colors.backgroundBlack
                    },
                ),
        ) {
            SCIconButton(
                modifier = Modifier.wrapContentSize(),
                icon = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "Back",
                onClick = {},
                style = variant.first,
                size = variant.second,
            )
        }
    }
}
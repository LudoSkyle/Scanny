package fr.skyle.scanny.designsystem.core.button

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

@Composable
fun SCButton(
    text: String,
    onClick: () -> Unit,
    style: SCButtonDefaults.Style,
    size: SCButtonDefaults.Size,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    Button(
        modifier = modifier.heightIn(min = size.height),
        onClick = onClick,
        enabled = isEnabled,
        shape = SCTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = SCTheme.colors.backgroundDisabled,
            disabledContentColor = SCTheme.colors.textDisabled,
        ),
    ) {
        SCText(
            modifier = Modifier.wrapContentWidth(),
            text = text,
            style = SCTheme.typography.button,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private class SCButtonVariantProvider : PreviewParameterProvider<Pair<SCButtonDefaults.Style, SCButtonDefaults.Size>> {
    override val values =
        SCButtonDefaults.Style.entries.flatMap { style ->
            SCButtonDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCButton(
    @PreviewParameter(SCButtonVariantProvider::class) variant: Pair<SCButtonDefaults.Style, SCButtonDefaults.Size>,
) {
    SCTheme {
        SCButton(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = 16.dp),
            text = "Allow the camera",
            onClick = {},
            style = variant.first,
            size = variant.second,
        )
    }
}
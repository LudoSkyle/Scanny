package fr.skyle.scanny.designsystem.core.text

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A text in a style of [SCTheme.typography], in the content color of its container unless told otherwise. */
@Composable
fun SCText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCText() {
    SCTheme {
        SCText(
            modifier = Modifier.wrapContentSize(),
            text = "Scanny",
            style = SCTheme.typography.h1,
            color = SCTheme.colors.textDark,
        )
    }
}
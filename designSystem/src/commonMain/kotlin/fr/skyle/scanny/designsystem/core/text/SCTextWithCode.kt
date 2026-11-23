package fr.skyle.scanny.designsystem.core.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

private const val CODE_MARK = '`'

/**
 * A text whose parts between backticks are code, a value to read character by character: in the
 * monospaced [SCTheme.typography] `code`, on a tint. The backticks are not shown.
 */
@Composable
fun SCTextWithCode(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    val code = SCTheme.typography.code
    // The size of the text around it: a monospaced font looks larger than Lato at the same size
    val codeStyle = SpanStyle(
        fontFamily = code.fontFamily,
        fontWeight = code.fontWeight,
        background = SCTheme.colors.backgroundIcon,
    )
    val annotatedText = remember(text, codeStyle) { text.withCodeSpans(codeStyle = codeStyle) }

    Text(
        modifier = modifier,
        text = annotatedText,
        style = style,
        color = color,
    )
}

/** This text without its backticks, the parts between them in [codeStyle]. */
internal fun String.withCodeSpans(codeStyle: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        split(CODE_MARK).forEachIndexed { index, part ->
            // Odd parts sit between two marks
            if (index % 2 == 1) {
                withStyle(style = codeStyle) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }

@Preview(showBackground = true)
@Composable
private fun PreviewSCTextWithCode() {
    SCTheme {
        SCTextWithCode(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            text = "`96385074`: `963` for the prefix, `8507` for the product, `4` for the check.",
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )
    }
}

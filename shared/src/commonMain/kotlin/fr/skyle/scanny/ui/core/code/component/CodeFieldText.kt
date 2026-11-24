package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.hidden_password
import fr.skyle.scanny.resources.hide_password
import fr.skyle.scanny.resources.ic_visibility
import fr.skyle.scanny.resources.ic_visibility_off
import fr.skyle.scanny.resources.message
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.show_password
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A piece of the content, named unless it is the content itself, cut after [maxLines] lines. A
 * [isSecret] value, a password, is hidden until touched: the screen may be seen by others.
 */
@Composable
fun CodeFieldText(
    label: String?,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    isSecret: Boolean = false,
) {
    Column(modifier = modifier) {
        if (label != null) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
            )

            Spacer(modifier = Modifier.height(2.dp))
        }

        if (isSecret) {
            SecretValueText(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                maxLines = maxLines,
            )
        } else {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = value,
                style = SCTheme.typography.body1,
                color = SCTheme.colors.textDark,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// The value as dots, always as many to tell nothing of its length, shown or hidden again by a touch
@Composable
private fun SecretValueText(
    value: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
) {
    var isShown by rememberSaveable(value) { mutableStateOf(false) }
    val actionText = stringResource(if (isShown) Res.string.hide_password else Res.string.show_password)

    Row(
        modifier = modifier
            // Large enough to be touched, as a button
            .heightIn(min = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = actionText,
                onClick = { isShown = !isShown },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCText(
            modifier = Modifier.weight(
                weight = 1f,
                fill = false,
            ),
            text = if (isShown) value else stringResource(Res.string.hidden_password),
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(if (isShown) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility),
            contentDescription = actionText,
            colorFilter = ColorFilter.tint(color = SCTheme.colors.text),
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeFieldText() {
    SCTheme {
        CodeFieldText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = stringResource(Res.string.message),
            value = "See you at eight in front of the cinema",
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeFieldText_Secret() {
    SCTheme {
        CodeFieldText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = stringResource(Res.string.password),
            value = "soleil-2026",
            isSecret = true,
        )
    }
}

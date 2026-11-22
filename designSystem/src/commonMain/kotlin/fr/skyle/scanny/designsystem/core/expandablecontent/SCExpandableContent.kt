package fr.skyle.scanny.designsystem.core.expandablecontent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.collapsed
import fr.skyle.scanny.designsystem.resources.expanded
import fr.skyle.scanny.designsystem.resources.ic_chevron_right
import fr.skyle.scanny.designsystem.theme.SCTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// The chevron points down to unfold, up to fold
private const val COLLAPSED_ROTATION = 90f
private const val EXPANDED_ROTATION = 270f

/**
 * A part of a form few need, folded under a row named by its [label]: a touch unfolds the [content]
 * under the row, spaced as the fields of a form, another folds it. Inside a card or a column, which
 * bring the margins.
 */
@Composable
fun SCExpandableContent(
    label: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val chevronRotation by animateFloatAsState(if (isExpanded) EXPANDED_ROTATION else COLLAPSED_ROTATION)
    val state = stringResource(if (isExpanded) Res.string.expanded else Res.string.collapsed)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(SCTheme.shapes.medium)
                .clickable(
                    role = Role.Button,
                    onClick = { onExpandedChange(!isExpanded) },
                )
                .semantics { stateDescription = state }
                .heightIn(min = 48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SCText(
                text = label,
                style = SCTheme.typography.body3,
                color = SCTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation),
                painter = painterResource(Res.drawable.ic_chevron_right),
                // Said by the state of the row
                contentDescription = null,
                tint = SCTheme.colors.textPrimary,
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                content = content,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCExpandableContent() {
    SCTheme {
        SCExpandableContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = "More options",
            isExpanded = true,
            onExpandedChange = {},
        ) {
            SCText(
                text = "A field few need",
                style = SCTheme.typography.body1,
            )
        }
    }
}

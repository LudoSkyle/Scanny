package fr.skyle.scanny.designsystem.core.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
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
 * A titled block of a screen folded under its title, for a content not everyone needs: a touch on the
 * title unfolds its [SCCard], another folds it.
 */
@Composable
fun SCExpandableCardStructure(
    title: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    style: SCCardDefaults.Style,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val chevronRotation by animateFloatAsState(if (isExpanded) EXPANDED_ROTATION else COLLAPSED_ROTATION)
    val state = stringResource(if (isExpanded) Res.string.expanded else Res.string.collapsed)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SCTheme.shapes.medium)
                .clickable(
                    role = Role.Button,
                    onClick = { onExpandedChange(!isExpanded) },
                )
                .semantics {
                    heading()
                    stateDescription = state
                }
                .padding(
                    horizontal = 4.dp,
                    vertical = 4.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SCText(
                modifier = Modifier.weight(1f),
                text = title,
                style = SCTheme.typography.subtitle1,
                color = SCTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .rotate(chevronRotation),
                painter = painterResource(Res.drawable.ic_chevron_right),
                // Said by the state of the row
                contentDescription = null,
                tint = SCTheme.colors.text,
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                SCCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    content = content,
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF5F6FC,
)
@Composable
private fun PreviewSCExpandableCardStructure() {
    SCTheme {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            SCExpandableCardStructure(
                modifier = Modifier.fillMaxWidth(),
                title = "Folded",
                isExpanded = false,
                onExpandedChange = {},
                style = SCCardDefaults.Style.LIGHT,
            ) {
                SCText(
                    modifier = Modifier.padding(all = 16.dp),
                    text = "Content",
                    style = SCTheme.typography.body1,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SCExpandableCardStructure(
                modifier = Modifier.fillMaxWidth(),
                title = "Unfolded",
                isExpanded = true,
                onExpandedChange = {},
                style = SCCardDefaults.Style.LIGHT,
            ) {
                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    text = "Content",
                    style = SCTheme.typography.body1,
                )
            }
        }
    }
}

package fr.skyle.scanny.designsystem.core.topappbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.back
import fr.skyle.scanny.designsystem.resources.ic_arrow_back
import fr.skyle.scanny.designsystem.theme.SCTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SCTopAppBar(
    style: SCTopAppBarDefaults.Style,
    modifier: Modifier = Modifier,
    title: String? = null,
    // Under the title: where the screen stands in a flow, for instance
    subtitle: String? = null,
    onBackClicked: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        style = SCTheme.typography.h3,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    if (subtitle != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = subtitle,
                            style = SCTheme.typography.subtitle2,
                            color = style.subtitleColor,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (onBackClicked != null) {
                SCIconButton(
                    icon = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.back),
                    onClick = onBackClicked,
                    style = style.iconButtonStyle,
                    size = SCIconButtonDefaults.Size.MEDIUM,
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = style.containerColor,
            titleContentColor = style.contentColor,
            navigationIconContentColor = style.contentColor,
            actionIconContentColor = style.contentColor,
        ),
    )
}

private class SCTopAppBarStyleProvider : PreviewParameterProvider<SCTopAppBarDefaults.Style> {
    override val values = SCTopAppBarDefaults.Style.entries.asSequence()
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PreviewSCTopAppBar(
    @PreviewParameter(SCTopAppBarStyleProvider::class) style: SCTopAppBarDefaults.Style,
) {
    SCTheme {
        SCTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            style = style,
            title = "Scanny",
            subtitle = "Step 1 of 2",
            onBackClicked = {},
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PreviewSCTopAppBar_WithoutTitle(
    @PreviewParameter(SCTopAppBarStyleProvider::class) style: SCTopAppBarDefaults.Style,
) {
    SCTheme {
        SCTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            style = style,
        )
    }
}
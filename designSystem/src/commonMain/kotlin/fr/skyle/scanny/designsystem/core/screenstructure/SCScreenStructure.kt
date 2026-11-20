package fr.skyle.scanny.designsystem.core.screenstructure

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme

@Composable
fun SCScreenStructure(
    style: SCScreenStructureDefaults.Style,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = style.containerColor,
        contentColor = style.contentColor,
        contentWindowInsets = WindowInsets.safeDrawing,
        content = content,
    )
}

private class SCScreenStructureStyleProvider : PreviewParameterProvider<SCScreenStructureDefaults.Style> {
    override val values = SCScreenStructureDefaults.Style.entries.asSequence()
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun PreviewSCScreenStructure(
    @PreviewParameter(SCScreenStructureStyleProvider::class) style: SCScreenStructureDefaults.Style,
) {
    SCTheme {
        SCScreenStructure(
            modifier = Modifier.fillMaxSize(),
            style = style,
            topBar = {
                SCTopAppBar(
                    style = when (style) {
                        SCScreenStructureDefaults.Style.BACKGROUND -> SCTopAppBarDefaults.Style.BACKGROUND
                        SCScreenStructureDefaults.Style.BLACK -> SCTopAppBarDefaults.Style.TRANSPARENT
                    },
                    title = "Scanny",
                    onBackClicked = {},
                )
            },
        ) { contentPadding ->
            Text(
                modifier = Modifier
                    .padding(contentPadding)
                    .wrapContentSize(),
                text = "Content",
            )
        }
    }
}
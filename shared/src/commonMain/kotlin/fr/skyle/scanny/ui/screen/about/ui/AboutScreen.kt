package fr.skyle.scanny.ui.screen.about.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCard
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.about
import fr.skyle.scanny.resources.app_description
import fr.skyle.scanny.resources.app_name
import fr.skyle.scanny.resources.created_by
import fr.skyle.scanny.resources.created_by_skyle
import fr.skyle.scanny.resources.github
import fr.skyle.scanny.resources.ic_open_in_new
import fr.skyle.scanny.resources.scanny_logo
import fr.skyle.scanny.resources.thanks_for_using_app
import fr.skyle.scanny.resources.version_and_build
import fr.skyle.scanny.ui.core.link.component.LinkRow
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(
    versionName: String,
    build: String,
    onBackClicked: () -> Unit,
    onGithubClicked: () -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.about),
                onBackClicked = onBackClicked,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(contentPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 24.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.size(96.dp),
                painter = painterResource(Res.drawable.scanny_logo),
                contentDescription = stringResource(Res.string.app_name),
            )

            Spacer(modifier = Modifier.height(16.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.app_name),
                style = SCTheme.typography.h1,
                color = SCTheme.colors.textDark,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.version_and_build, versionName, build),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SCCard(
                modifier = Modifier.fillMaxWidth(),
                style = SCCardDefaults.Style.LIGHT,
            ) {
                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    text = stringResource(Res.string.app_description),
                    style = SCTheme.typography.body2,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SCCard(
                modifier = Modifier.fillMaxWidth(),
                style = SCCardDefaults.Style.LIGHT,
            ) {
                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                        ),
                    text = stringResource(Res.string.created_by),
                    style = SCTheme.typography.caption,
                    color = SCTheme.colors.text,
                )

                SCText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 2.dp,
                            end = 16.dp,
                            bottom = 8.dp,
                        ),
                    text = stringResource(Res.string.created_by_skyle),
                    style = SCTheme.typography.body1,
                )

                LinkRow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.github),
                    icon = painterResource(Res.drawable.ic_open_in_new),
                    onClick = onGithubClicked,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.thanks_for_using_app),
                style = SCTheme.typography.body2,
                color = SCTheme.colors.text,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewAboutScreen() {
    SCTheme {
        AboutScreen(
            versionName = "2.0",
            build = "20",
            onBackClicked = {},
            onGithubClicked = {},
        )
    }
}
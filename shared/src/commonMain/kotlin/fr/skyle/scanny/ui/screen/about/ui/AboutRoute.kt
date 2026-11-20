package fr.skyle.scanny.ui.screen.about.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.scanny.ui.core.link.ScannyLinks
import fr.skyle.scanny.ui.core.link.openLink
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AboutRoute(
    navigateBack: () -> Unit,
    viewModel: AboutViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AboutScreen(
        versionName = uiState.versionName,
        build = uiState.build,
        onBackClicked = navigateBack,
        // Every device has a browser: a failure is only logged
        onGithubClicked = { uriHandler.openLink(link = ScannyLinks.GITHUB) },
    )
}
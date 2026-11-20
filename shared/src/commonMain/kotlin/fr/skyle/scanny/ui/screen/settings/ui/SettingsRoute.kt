package fr.skyle.scanny.ui.screen.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.app.rateAppLinks
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.history_cleared
import fr.skyle.scanny.resources.history_not_cleared
import fr.skyle.scanny.resources.no_app_to_open
import fr.skyle.scanny.resources.setting_not_saved
import fr.skyle.scanny.ui.core.history.component.ClearHistoryDialog
import fr.skyle.scanny.ui.core.link.ScannyLinks
import fr.skyle.scanny.ui.core.link.openLink
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    navigateToFeedback: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isClearHistoryDialogVisible by rememberSaveable { mutableStateOf(false) }

    val openLinks: (List<String>) -> Unit = { links ->
        // The first link the device opens: the store app, else its website
        if (links.none { link -> uriHandler.openLink(link = link) }) {
            coroutineScope.launch {
                snackbarState.show(message = getString(Res.string.no_app_to_open))
            }
        }
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    SettingsViewModel.SettingsEvent.HistoryCleared -> launch {
                        snackbarState.show(message = getString(Res.string.history_cleared))
                    }

                    SettingsViewModel.SettingsEvent.HistoryNotCleared -> launch {
                        snackbarState.show(message = getString(Res.string.history_not_cleared))
                    }

                    SettingsViewModel.SettingsEvent.SettingNotSaved -> launch {
                        snackbarState.show(message = getString(Res.string.setting_not_saved))
                    }
                }
            }
        }
    }

    SettingsScreen(
        settings = uiState.settings,
        versionName = uiState.versionName,
        build = uiState.build,
        isRateAppAvailable = rateAppLinks.isNotEmpty(),
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onSettingChanged = viewModel::onSettingChanged,
        onClearHistoryClicked = {
            isClearHistoryDialogVisible = true
        },
        onStarProjectClicked = { openLinks(listOf(ScannyLinks.GITHUB)) },
        onFeedbackClicked = navigateToFeedback,
        onRateAppClicked = { openLinks(rateAppLinks) },
        onPrivacyPolicyClicked = { openLinks(listOf(ScannyLinks.PRIVACY_POLICY)) },
        onOpeniumClicked = { openLinks(listOf(ScannyLinks.OPENIUM)) },
    )

    if (isClearHistoryDialogVisible) {
        ClearHistoryDialog(
            onConfirmClicked = {
                isClearHistoryDialogVisible = false
                viewModel.onClearHistoryConfirmed()
            },
            onDismissRequest = {
                isClearHistoryDialogVisible = false
            },
        )
    }
}
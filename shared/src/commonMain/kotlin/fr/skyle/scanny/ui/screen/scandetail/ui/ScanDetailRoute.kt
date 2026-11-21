package fr.skyle.scanny.ui.screen.scandetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.clipboard.isCopyConfirmedBySystem
import fr.skyle.scanny.platform.clipboard.textClipEntry
import fr.skyle.scanny.platform.wifi.rememberWifiNetworkJoiner
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_not_deleted
import fr.skyle.scanny.resources.copied_to_clipboard
import fr.skyle.scanny.resources.no_app_to_open
import fr.skyle.scanny.ui.core.code.action.open
import fr.skyle.scanny.ui.core.code.component.DeleteCodeDialog
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.screen.scandetail.ui.component.CodeImageDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ScanDetailRoute(
    codeId: Long,
    navigateBack: () -> Unit,
    viewModel: ScanDetailViewModel = koinViewModel(parameters = { parametersOf(codeId) }),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val joinWifiNetwork = rememberWifiNetworkJoiner()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isCodeImageDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    ScanDetailViewModel.ScanDetailEvent.CodeDeleted -> {
                        navigateBack()
                    }

                    ScanDetailViewModel.ScanDetailEvent.DeleteFailed -> launch {
                        snackbarState.show(message = getString(Res.string.code_not_deleted))
                    }

                    ScanDetailViewModel.ScanDetailEvent.ContactNotAdded -> launch {
                        snackbarState.show(message = getString(Res.string.no_app_to_open))
                    }
                }
            }
        }
    }

    ScanDetailScreen(
        code = uiState.code,
        isDeleting = uiState.isDeleting,
        snackbarState = snackbarState,
        onShowCodeClicked = {
            isCodeImageDialogVisible = true
        },
        onBackClicked = navigateBack,
        onShareClicked = viewModel::onShareClicked,
        onDeleteClicked = {
            isDeleteDialogVisible = true
        },
        onCopyClicked = { text ->
            coroutineScope.launch {
                clipboard.setClipEntry(textClipEntry(text = text))
                if (!isCopyConfirmedBySystem) {
                    snackbarState.show(message = getString(Res.string.copied_to_clipboard))
                }
            }
        },
        onActionClicked = { action ->
            when (action) {
                is CodeAction.Link -> if (!uriHandler.open(action = action)) {
                    coroutineScope.launch {
                        snackbarState.show(message = getString(Res.string.no_app_to_open))
                    }
                }

                is CodeAction.AddContact -> viewModel.onAddContactClicked(contact = action.contact)
                is CodeAction.JoinWifi -> joinWifiNetwork(action.wifi)
            }
        },
    )

    val code = uiState.code
    val format = code?.format
    if (isCodeImageDialogVisible && code != null && format != null) {
        CodeImageDialog(
            rawValue = code.rawValue,
            format = format,
            onDismissRequest = {
                isCodeImageDialogVisible = false
            },
        )
    }

    if (isDeleteDialogVisible) {
        DeleteCodeDialog(
            onConfirmClicked = {
                isDeleteDialogVisible = false
                viewModel.onDeleteConfirmed()
            },
            onDismissRequest = {
                isDeleteDialogVisible = false
            },
        )
    }
}
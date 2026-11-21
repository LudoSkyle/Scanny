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
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.platform.clipboard.isCopyConfirmedBySystem
import fr.skyle.scanny.platform.clipboard.textClipEntry
import fr.skyle.scanny.platform.wifi.rememberWifiNetworkJoiner
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_not_deleted
import fr.skyle.scanny.resources.copied_to_clipboard
import fr.skyle.scanny.resources.image_not_saved
import fr.skyle.scanny.resources.image_not_shared
import fr.skyle.scanny.resources.no_app_to_open
import fr.skyle.scanny.ui.core.code.action.open
import fr.skyle.scanny.ui.core.code.component.CodeImageSheet
import fr.skyle.scanny.ui.core.code.component.DeleteCodeDialog
import fr.skyle.scanny.ui.core.code.model.CodeAction
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ScanDetailRoute(
    codeId: Long,
    navigateToCreateCode: () -> Unit,
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
    var isCodeSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isImageSaved by rememberSaveable { mutableStateOf(false) }

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

                    // Told by the button of the sheet: a message of the screen would show under it
                    ScanDetailViewModel.ScanDetailEvent.ImageSaved -> {
                        isImageSaved = true
                    }

                    // The sheet closed for the message to be seen
                    ScanDetailViewModel.ScanDetailEvent.ImageNotSaved -> {
                        isCodeSheetVisible = false
                        launch {
                            snackbarState.show(message = getString(Res.string.image_not_saved))
                        }
                    }

                    ScanDetailViewModel.ScanDetailEvent.ImageNotShared -> {
                        isCodeSheetVisible = false
                        launch {
                            snackbarState.show(message = getString(Res.string.image_not_shared))
                        }
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
            isImageSaved = false
            isCodeSheetVisible = true
        },
        onEditClicked = navigateToCreateCode,
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
    if (isCodeSheetVisible && code != null && format != null) {
        CodeImageSheet(
            rawValue = code.rawValue,
            format = format,
            // A scanned code is drawn again dark on white, as any scanner reads it
            style = CodeStyle(),
            canSave = uiState.canSaveImage,
            isSaved = isImageSaved,
            onShareClicked = viewModel::onShareImageClicked,
            onSaveClicked = viewModel::onSaveImageClicked,
            onDismissRequest = {
                isCodeSheetVisible = false
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
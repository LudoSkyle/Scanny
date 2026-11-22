package fr.skyle.scanny.ui.screen.creationdetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.clipboard.isCopyConfirmedBySystem
import fr.skyle.scanny.platform.clipboard.textClipEntry
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_not_deleted
import fr.skyle.scanny.resources.copied_to_clipboard
import fr.skyle.scanny.resources.image_not_saved
import fr.skyle.scanny.resources.image_not_shared
import fr.skyle.scanny.ui.core.code.component.CodeImageSheet
import fr.skyle.scanny.ui.core.code.component.DeleteCodeDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreationDetailRoute(
    creationId: Long,
    navigateToCreateCode: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: CreationDetailViewModel = koinViewModel(parameters = { parametersOf(creationId) }),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isCodeSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isImageSaved by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    CreationDetailViewModel.CreationDetailEvent.CreationDeleted -> {
                        navigateBack()
                    }

                    CreationDetailViewModel.CreationDetailEvent.DeleteFailed -> launch {
                        snackbarState.show(message = getString(Res.string.code_not_deleted))
                    }

                    // Told by the button of the sheet: a message of the screen would show under it
                    CreationDetailViewModel.CreationDetailEvent.ImageSaved -> {
                        isImageSaved = true
                    }

                    // The sheet closed for the message to be seen
                    CreationDetailViewModel.CreationDetailEvent.ImageNotSaved -> {
                        isCodeSheetVisible = false
                        launch {
                            snackbarState.show(message = getString(Res.string.image_not_saved))
                        }
                    }

                    CreationDetailViewModel.CreationDetailEvent.ImageNotShared -> {
                        isCodeSheetVisible = false
                        launch {
                            snackbarState.show(message = getString(Res.string.image_not_shared))
                        }
                    }
                }
            }
        }
    }

    CreationDetailScreen(
        creation = uiState.creation,
        isDeleting = uiState.isDeleting,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onShowCodeClicked = {
            isImageSaved = false
            isCodeSheetVisible = true
        },
        onEditClicked = navigateToCreateCode,
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
    )

    val creation = uiState.creation
    val format = creation?.format
    if (isCodeSheetVisible && creation != null && format != null) {
        CodeImageSheet(
            rawValue = creation.rawValue,
            format = format,
            style = creation.style,
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
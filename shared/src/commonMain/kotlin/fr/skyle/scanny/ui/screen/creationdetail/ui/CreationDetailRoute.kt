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
import fr.skyle.scanny.resources.image_saved
import fr.skyle.scanny.ui.core.code.component.DeleteCodeDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreationDetailRoute(
    creationId: Long,
    navigateBack: () -> Unit,
    viewModel: CreationDetailViewModel = koinViewModel(parameters = { parametersOf(creationId) }),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

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

                    CreationDetailViewModel.CreationDetailEvent.ImageSaved -> launch {
                        snackbarState.show(message = getString(Res.string.image_saved))
                    }

                    CreationDetailViewModel.CreationDetailEvent.ImageNotSaved -> launch {
                        snackbarState.show(message = getString(Res.string.image_not_saved))
                    }

                    CreationDetailViewModel.CreationDetailEvent.ImageNotShared -> launch {
                        snackbarState.show(message = getString(Res.string.image_not_shared))
                    }
                }
            }
        }
    }

    CreationDetailScreen(
        creation = uiState.creation,
        canSaveImage = uiState.canSaveImage,
        isDeleting = uiState.isDeleting,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onShareClicked = viewModel::onShareClicked,
        onSaveClicked = viewModel::onSaveClicked,
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
package fr.skyle.scanny.ui.screen.scangroup.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.favorite_not_changed
import fr.skyle.scanny.resources.group_not_deleted
import fr.skyle.scanny.ui.core.code.component.DeleteGroupDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ScanGroupRoute(
    scanId: Long,
    navigateToScanDetail: (codeId: Long) -> Unit,
    navigateBack: () -> Unit,
    viewModel: ScanGroupViewModel = koinViewModel(parameters = { parametersOf(scanId) }),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    ScanGroupViewModel.ScanGroupEvent.GroupGone -> {
                        navigateBack()
                    }

                    ScanGroupViewModel.ScanGroupEvent.DeleteFailed -> launch {
                        snackbarState.show(message = getString(Res.string.group_not_deleted))
                    }

                    ScanGroupViewModel.ScanGroupEvent.FavoriteNotChanged -> launch {
                        snackbarState.show(message = getString(Res.string.favorite_not_changed))
                    }
                }
            }
        }
    }

    ScanGroupScreen(
        group = uiState.group,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onFavoriteClicked = viewModel::onFavoriteClicked,
        onShareClicked = viewModel::onShareClicked,
        onDeleteClicked = {
            isDeleteDialogVisible = true
        },
        onCodeClicked = navigateToScanDetail,
    )

    if (isDeleteDialogVisible) {
        DeleteGroupDialog(
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
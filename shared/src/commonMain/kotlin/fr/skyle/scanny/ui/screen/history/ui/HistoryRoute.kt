package fr.skyle.scanny.ui.screen.history.ui

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
import fr.skyle.scanny.resources.code_not_deleted
import fr.skyle.scanny.resources.favorite_not_changed
import fr.skyle.scanny.resources.history_not_cleared
import fr.skyle.scanny.ui.core.code.component.DeleteCodeDialog
import fr.skyle.scanny.ui.core.code.component.DeleteGroupDialog
import fr.skyle.scanny.ui.screen.history.ui.component.ClearHistoryDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(
    navigateToScanDetail: (codeId: Long) -> Unit,
    navigateToScanGroup: (scanId: Long) -> Unit,
    navigateToCreationDetail: (creationId: Long) -> Unit,
    navigateBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isClearDialogVisible by rememberSaveable { mutableStateOf(false) }
    // What a held row asked to delete, confirmed first: a capture, of one code or a group, or a creation
    var scanIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    var isGroupToDelete by rememberSaveable { mutableStateOf(false) }
    var creationIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    HistoryViewModel.HistoryEvent.ClearFailed -> launch {
                        snackbarState.show(message = getString(Res.string.history_not_cleared))
                    }

                    HistoryViewModel.HistoryEvent.FavoriteNotChanged -> launch {
                        snackbarState.show(message = getString(Res.string.favorite_not_changed))
                    }

                    HistoryViewModel.HistoryEvent.DeleteFailed -> launch {
                        snackbarState.show(message = getString(Res.string.code_not_deleted))
                    }

                    is HistoryViewModel.HistoryEvent.OpenScanDetail -> {
                        navigateToScanDetail(event.codeId)
                    }

                    is HistoryViewModel.HistoryEvent.OpenScanGroup -> {
                        navigateToScanGroup(event.scanId)
                    }

                    is HistoryViewModel.HistoryEvent.OpenCreationDetail -> {
                        navigateToCreationDetail(event.creationId)
                    }
                }
            }
        }
    }

    HistoryScreen(
        selectedView = uiState.selectedView,
        scanSections = uiState.scanSections,
        creationSections = uiState.creationSections,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onViewSelected = viewModel::onViewSelected,
        onClearClicked = {
            isClearDialogVisible = true
        },
        onScanClicked = viewModel::onScanClicked,
        onCreationClicked = viewModel::onCreationClicked,
        onScanFavoriteClicked = viewModel::onScanFavoriteClicked,
        onScanDeleteClicked = { item ->
            scanIdToDelete = item.scanId
            isGroupToDelete = item.codeCount > 1
        },
        onCreationFavoriteClicked = viewModel::onCreationFavoriteClicked,
        onCreationDeleteClicked = { item ->
            creationIdToDelete = item.creationId
        },
    )

    scanIdToDelete?.let { scanId ->
        val onConfirmClicked = {
            scanIdToDelete = null
            viewModel.onScanDeleteConfirmed(scanId = scanId)
        }
        val onDismissRequest = {
            scanIdToDelete = null
        }
        if (isGroupToDelete) {
            DeleteGroupDialog(
                onConfirmClicked = onConfirmClicked,
                onDismissRequest = onDismissRequest,
            )
        } else {
            DeleteCodeDialog(
                onConfirmClicked = onConfirmClicked,
                onDismissRequest = onDismissRequest,
            )
        }
    }

    creationIdToDelete?.let { creationId ->
        DeleteCodeDialog(
            onConfirmClicked = {
                creationIdToDelete = null
                viewModel.onCreationDeleteConfirmed(creationId = creationId)
            },
            onDismissRequest = {
                creationIdToDelete = null
            },
        )
    }

    if (isClearDialogVisible) {
        ClearHistoryDialog(
            view = uiState.selectedView,
            onConfirmClicked = {
                isClearDialogVisible = false
                viewModel.onClearConfirmed()
            },
            onDismissRequest = {
                isClearDialogVisible = false
            },
        )
    }
}
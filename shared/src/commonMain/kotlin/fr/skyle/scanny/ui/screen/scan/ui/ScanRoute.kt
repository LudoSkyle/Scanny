package fr.skyle.scanny.ui.screen.scan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import co.touchlab.kermit.Logger
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.camera.CameraPermissionStatus
import fr.skyle.scanny.platform.camera.rememberCameraPermissionState
import fr.skyle.scanny.platform.clipboard.textClipEntry
import fr.skyle.scanny.platform.gallery.rememberImageBarcodeScanner
import fr.skyle.scanny.platform.wifi.rememberWifiNetworkJoiner
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.no_code_in_image
import fr.skyle.scanny.resources.scan_not_saved
import fr.skyle.scanny.ui.core.code.action.open
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.screen.scan.ui.component.DiscardCodesDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanRoute(
    navigateToScanDetail: (codeId: Long) -> Unit,
    navigateToScanGroup: (scanId: Long) -> Unit,
    navigateToScanHistory: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: ScanViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val hapticFeedback = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberCameraPermissionState()
    val scanImage = rememberImageBarcodeScanner(onResult = viewModel::onImageScanned)
    val joinWifiNetwork = rememberWifiNetworkJoiner()
    val snackbarState = rememberSCSnackbarState()

    var isDiscardDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState) {
        if (cameraPermissionState.status == CameraPermissionStatus.UNKNOWN) {
            cameraPermissionState.request()
        }
    }

    // Composed again when back from another screen
    LaunchedEffect(Unit) {
        viewModel.onScreenShown()
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    ScanViewModel.ScanEvent.Vibrate -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    }

                    is ScanViewModel.ScanEvent.CopyToClipboard -> launch {
                        clipboard.setClipEntry(textClipEntry(text = event.text))
                    }

                    is ScanViewModel.ScanEvent.OpenUrl -> {
                        try {
                            uriHandler.openUri(event.url)
                        } catch (exception: Exception) {
                            // No app on the device opens this link
                            Logger.w(throwable = exception) { "Failed to open a scanned link" }
                        }
                    }

                    // Launched apart: a message stays a few seconds, the next events do not wait for it
                    ScanViewModel.ScanEvent.NoCodeInImage -> launch {
                        snackbarState.show(message = getString(Res.string.no_code_in_image))
                    }

                    ScanViewModel.ScanEvent.ScanNotSaved -> launch {
                        snackbarState.show(message = getString(Res.string.scan_not_saved))
                    }

                    is ScanViewModel.ScanEvent.OpenScanDetail -> {
                        navigateToScanDetail(event.codeId)
                    }

                    is ScanViewModel.ScanEvent.OpenScanGroup -> {
                        navigateToScanGroup(event.scanId)
                    }

                    ScanViewModel.ScanEvent.ConfirmCodesDiscard -> {
                        isDiscardDialogVisible = true
                    }
                }
            }
        }
    }

    ScanScreen(
        permissionStatus = cameraPermissionState.status,
        isCameraUnavailable = uiState.isCameraUnavailable,
        isDetectionPaused = uiState.isDetectionPaused,
        isTorchEnabled = uiState.isTorchEnabled,
        cameraCapabilities = uiState.cameraCapabilities,
        multipleScan = uiState.multipleScan,
        result = uiState.result,
        snackbarState = snackbarState,
        onBarcodeDetected = viewModel::onBarcodeDetected,
        onCameraCapabilitiesChanged = viewModel::onCameraCapabilitiesChanged,
        onCameraUnavailable = viewModel::onCameraUnavailable,
        onAllowCameraClicked = cameraPermissionState::request,
        onOpenSettingsClicked = cameraPermissionState::openAppSettings,
        onScanImageClicked = scanImage,
        onTorchClicked = viewModel::onTorchClicked,
        onMultipleScanClicked = viewModel::onMultipleScanClicked,
        onMultipleScanFinished = viewModel::onMultipleScanFinished,
        onHistoryClicked = navigateToScanHistory,
        onSettingsClicked = navigateToSettings,
        onResultActionClicked = { action ->
            when (action) {
                // Under the sheet, a message would not be seen: the failure is only logged
                is CodeAction.Link -> uriHandler.open(action = action)
                is CodeAction.AddContact -> viewModel.onAddContactClicked(contact = action.contact)
                is CodeAction.JoinWifi -> joinWifiNetwork(action.wifi)
            }
        },
        onCopyResultClicked = { text ->
            coroutineScope.launch {
                clipboard.setClipEntry(textClipEntry(text = text))
            }
        },
        onSeeResultDetailClicked = viewModel::onSeeDetailClicked,
        onResultCodeClicked = viewModel::onResultCodeClicked,
        onSeeResultGroupClicked = viewModel::onSeeGroupClicked,
        onResultDismissed = viewModel::onResultDismissed,
    )

    if (isDiscardDialogVisible) {
        DiscardCodesDialog(
            onConfirmClicked = {
                isDiscardDialogVisible = false
                viewModel.onMultipleScanDiscarded()
            },
            onDismissRequest = {
                isDiscardDialogVisible = false
            },
        )
    }
}
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.camera.CameraPermissionStatus
import fr.skyle.scanny.platform.camera.rememberCameraPermissionState
import fr.skyle.scanny.platform.clipboard.textClipEntry
import fr.skyle.scanny.platform.gallery.rememberImageBarcodeScanner
import fr.skyle.scanny.platform.wifi.WifiNetworkJoiner
import fr.skyle.scanny.platform.wifi.rememberWifiNetworkJoiner
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.no_app_to_open
import fr.skyle.scanny.resources.no_code_in_image
import fr.skyle.scanny.resources.scan_not_saved
import fr.skyle.scanny.ui.core.code.action.open
import fr.skyle.scanny.ui.core.code.mapper.showWifiJoinResult
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.screen.scan.ui.component.DiscardCodesDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanRoute(
    navigateToScanDetail: (codeId: Long) -> Unit,
    navigateToScanGroup: (scanId: Long) -> Unit,
    navigateToHistory: () -> Unit,
    navigateToCreateCode: () -> Unit,
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
    val snackbarState = rememberSCSnackbarState()
    // The messages about a result, in its sheet: a message of the screen would show under it
    val resultSnackbarState = rememberSCSnackbarState()
    val messages: () -> SCSnackbarState = { if (viewModel.uiState.value.result != null) resultSnackbarState else snackbarState }
    lateinit var wifiNetworkJoiner: WifiNetworkJoiner
    wifiNetworkJoiner = rememberWifiNetworkJoiner { wifi, result ->
        coroutineScope.launch {
            if (messages().showWifiJoinResult(wifi = wifi, result = result)) {
                wifiNetworkJoiner.joinFromSettings(wifi = wifi)
            }
        }
    }

    var isDiscardDialogVisible by rememberSaveable { mutableStateOf(false) }

    val openCodeAction: (CodeAction) -> Unit = { action ->
        when (action) {
            is CodeAction.Link -> if (!uriHandler.open(action = action)) {
                coroutineScope.launch {
                    messages().show(message = getString(Res.string.no_app_to_open))
                }
            }

            is CodeAction.AddContact -> viewModel.onAddContactClicked(contact = action.contact)
            is CodeAction.AddEvent -> viewModel.onAddEventClicked(event = action.event)
            is CodeAction.JoinWifi -> wifiNetworkJoiner.join(wifi = action.wifi)
        }
    }

    LaunchedEffect(cameraPermissionState) {
        if (cameraPermissionState.status == CameraPermissionStatus.UNKNOWN) {
            cameraPermissionState.request()
        }
    }

    // Resumed when back from another screen, from another app, or from a panel of the system
    LifecycleResumeEffect(Unit) {
        viewModel.onScreenResumed()
        onPauseOrDispose {
            viewModel.onScreenPaused()
        }
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

                    is ScanViewModel.ScanEvent.OpenContent -> {
                        openCodeAction(event.action)
                    }

                    // Launched apart: a message stays a few seconds, the next events do not wait for it
                    ScanViewModel.ScanEvent.NoCodeInImage -> launch {
                        snackbarState.show(message = getString(Res.string.no_code_in_image))
                    }

                    ScanViewModel.ScanEvent.ScanNotSaved -> launch {
                        snackbarState.show(message = getString(Res.string.scan_not_saved))
                    }

                    // On the sheet of the result when it shows one, on the scan otherwise
                    ScanViewModel.ScanEvent.ContactNotAdded,
                    ScanViewModel.ScanEvent.EventNotAdded,
                    -> launch {
                        messages().show(message = getString(Res.string.no_app_to_open))
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
        cameraLens = uiState.cameraLens,
        isTorchEnabled = uiState.isTorchEnabled,
        cameraCapabilities = uiState.cameraCapabilities,
        multipleScan = uiState.multipleScan,
        result = uiState.result,
        snackbarState = snackbarState,
        resultSnackbarState = resultSnackbarState,
        onBarcodeDetected = viewModel::onBarcodeDetected,
        onCameraCapabilitiesChanged = viewModel::onCameraCapabilitiesChanged,
        onCameraUnavailable = viewModel::onCameraUnavailable,
        onAllowCameraClicked = cameraPermissionState::request,
        onOpenSettingsClicked = cameraPermissionState::openAppSettings,
        onScanImageClicked = scanImage,
        onTorchClicked = viewModel::onTorchClicked,
        onMultipleScanClicked = viewModel::onMultipleScanClicked,
        onMultipleScanFinished = viewModel::onMultipleScanFinished,
        onCreateClicked = navigateToCreateCode,
        onHistoryClicked = navigateToHistory,
        onSettingsClicked = navigateToSettings,
        onResultActionClicked = openCodeAction,
        onCopyResultClicked = { text ->
            coroutineScope.launch {
                clipboard.setClipEntry(textClipEntry(text = text))
            }
        },
        onSeeResultDetailClicked = viewModel::onSeeDetailClicked,
        onResultCodeClicked = viewModel::onResultCodeClicked,
        onSeeResultGroupClicked = viewModel::onSeeGroupClicked,
        onResultDismissed = {
            resultSnackbarState.dismiss()
            viewModel.onResultDismissed()
        },
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
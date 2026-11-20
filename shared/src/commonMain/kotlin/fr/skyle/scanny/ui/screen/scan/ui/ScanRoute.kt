package fr.skyle.scanny.ui.screen.scan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import fr.skyle.scanny.platform.gallery.rememberImageBarcodeScanner
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.no_code_in_image
import fr.skyle.scanny.resources.scan_not_saved
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanRoute(
    viewModel: ScanViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val hapticFeedback = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberCameraPermissionState()
    val scanImage = rememberImageBarcodeScanner(onResult = viewModel::onImageScanned)
    val snackbarState = rememberSCSnackbarState()

    LaunchedEffect(cameraPermissionState) {
        if (cameraPermissionState.status == CameraPermissionStatus.UNKNOWN) {
            cameraPermissionState.request()
        }
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    ScanViewModel.ScanEvent.Vibrate -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
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
        result = uiState.result,
        snackbarState = snackbarState,
        onBarcodeDetected = viewModel::onBarcodeDetected,
        onCameraCapabilitiesChanged = viewModel::onCameraCapabilitiesChanged,
        onCameraUnavailable = viewModel::onCameraUnavailable,
        onAllowCameraClicked = cameraPermissionState::request,
        onOpenSettingsClicked = cameraPermissionState::openAppSettings,
        onScanImageClicked = scanImage,
        onTorchClicked = viewModel::onTorchClicked,
        onResultDismissed = viewModel::onResultDismissed,
    )
}
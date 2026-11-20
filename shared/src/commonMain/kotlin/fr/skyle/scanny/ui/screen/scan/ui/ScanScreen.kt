package fr.skyle.scanny.ui.screen.scan.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.DetectedBarcode
import fr.skyle.scanny.platform.camera.CameraCapabilities
import fr.skyle.scanny.platform.camera.CameraPermissionStatus
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.allow_camera
import fr.skyle.scanny.resources.camera_blocked
import fr.skyle.scanny.resources.camera_needed
import fr.skyle.scanny.resources.camera_unavailable
import fr.skyle.scanny.resources.open_settings
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.scan.ui.component.CameraMessage
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanCameraLayer
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanControls
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanResultSheet
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanScreen(
    permissionStatus: CameraPermissionStatus,
    isCameraUnavailable: Boolean,
    isDetectionPaused: Boolean,
    isTorchEnabled: Boolean,
    cameraCapabilities: CameraCapabilities?,
    result: ScanResultItem?,
    snackbarState: SCSnackbarState,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCameraCapabilitiesChanged: (CameraCapabilities) -> Unit,
    onCameraUnavailable: () -> Unit,
    onAllowCameraClicked: () -> Unit,
    onOpenSettingsClicked: () -> Unit,
    onScanImageClicked: () -> Unit,
    onTorchClicked: () -> Unit,
    onResultDismissed: () -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BLACK,
        snackbarState = snackbarState,
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            val messageModifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)

            when (permissionStatus) {
                CameraPermissionStatus.GRANTED -> if (isCameraUnavailable) {
                    CameraMessage(
                        modifier = messageModifier,
                        message = stringResource(Res.string.camera_unavailable),
                    )
                } else {
                    // Drawn under the system bars, as the rest of the screen is drawn over it
                    ScanCameraLayer(
                        modifier = Modifier.fillMaxSize(),
                        isDetectionEnabled = !isDetectionPaused,
                        isTorchEnabled = isTorchEnabled,
                        capabilities = cameraCapabilities,
                        onBarcodeDetected = onBarcodeDetected,
                        onCapabilitiesChanged = onCameraCapabilitiesChanged,
                        onCameraUnavailable = onCameraUnavailable,
                    )
                }

                CameraPermissionStatus.DENIED -> CameraMessage(
                    modifier = messageModifier,
                    message = stringResource(Res.string.camera_needed),
                    actionText = stringResource(Res.string.allow_camera),
                    onActionClicked = onAllowCameraClicked,
                )

                CameraPermissionStatus.DENIED_FOREVER -> CameraMessage(
                    modifier = messageModifier,
                    message = stringResource(Res.string.camera_blocked),
                    actionText = stringResource(Res.string.open_settings),
                    onActionClicked = onOpenSettingsClicked,
                )

                // The system asks for the permission over the black screen
                CameraPermissionStatus.UNKNOWN -> Unit
            }

            // Scanning an image needs no camera: it stays available whatever the permission
            ScanControls(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(contentPadding)
                    .padding(bottom = 32.dp)
                    .wrapContentSize(),
                hasTorch = permissionStatus == CameraPermissionStatus.GRANTED && cameraCapabilities?.hasTorch == true,
                isTorchEnabled = isTorchEnabled,
                onScanImageClicked = onScanImageClicked,
                onTorchClicked = onTorchClicked,
            )
        }
    }

    if (result != null) {
        ScanResultSheet(
            modifier = Modifier.fillMaxWidth(),
            result = result,
            onDismissRequest = onResultDismissed,
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanScreen() {
    SCTheme {
        ScanScreen(
            permissionStatus = CameraPermissionStatus.GRANTED,
            isCameraUnavailable = false,
            isDetectionPaused = false,
            isTorchEnabled = false,
            cameraCapabilities = CameraCapabilities(
                hasTorch = true,
                minZoomRatio = 1f,
                maxZoomRatio = 8f,
            ),
            result = null,
            snackbarState = rememberSCSnackbarState(),
            onBarcodeDetected = {},
            onCameraCapabilitiesChanged = {},
            onCameraUnavailable = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            onScanImageClicked = {},
            onTorchClicked = {},
            onResultDismissed = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanScreen_CameraBlocked() {
    SCTheme {
        ScanScreen(
            permissionStatus = CameraPermissionStatus.DENIED_FOREVER,
            isCameraUnavailable = false,
            isDetectionPaused = false,
            isTorchEnabled = false,
            cameraCapabilities = null,
            result = null,
            snackbarState = rememberSCSnackbarState(),
            onBarcodeDetected = {},
            onCameraCapabilitiesChanged = {},
            onCameraUnavailable = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            onScanImageClicked = {},
            onTorchClicked = {},
            onResultDismissed = {},
        )
    }
}
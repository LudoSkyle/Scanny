package fr.skyle.scanny.ui.screen.scan.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import fr.skyle.scanny.platform.camera.CameraLens
import fr.skyle.scanny.platform.camera.CameraPermissionStatus
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.allow_camera
import fr.skyle.scanny.resources.camera_blocked
import fr.skyle.scanny.resources.camera_needed
import fr.skyle.scanny.resources.camera_unavailable
import fr.skyle.scanny.resources.open_settings
import fr.skyle.scanny.resources.pass_codes_in_front_of_camera
import fr.skyle.scanny.resources.place_code_in_frame
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.scan.ui.component.CameraMessage
import fr.skyle.scanny.ui.screen.scan.ui.component.MultipleScanBar
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanCameraLayer
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanControls
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanGroupResultSheet
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanResultSheet
import fr.skyle.scanny.ui.screen.scan.ui.component.ScanTopActions
import fr.skyle.scanny.ui.screen.scan.ui.model.MultipleScanSession
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResult
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanScreen(
    permissionStatus: CameraPermissionStatus,
    isCameraUnavailable: Boolean,
    isDetectionPaused: Boolean,
    cameraLens: CameraLens,
    isTorchEnabled: Boolean,
    cameraCapabilities: CameraCapabilities?,
    multipleScan: MultipleScanSession?,
    result: ScanResult?,
    snackbarState: SCSnackbarState,
    resultSnackbarState: SCSnackbarState,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCameraCapabilitiesChanged: (CameraCapabilities) -> Unit,
    onCameraUnavailable: () -> Unit,
    onAllowCameraClicked: () -> Unit,
    onOpenSettingsClicked: () -> Unit,
    onScanImageClicked: () -> Unit,
    onTorchClicked: () -> Unit,
    onMultipleScanClicked: () -> Unit,
    onMultipleScanFinished: () -> Unit,
    onCreateClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onResultActionClicked: (CodeAction) -> Unit,
    onCopyResultClicked: (text: String) -> Unit,
    onSeeResultDetailClicked: () -> Unit,
    onResultCodeClicked: (codeId: Long) -> Unit,
    onSeeResultGroupClicked: () -> Unit,
    onResultDismissed: () -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BLACK,
        // The controls under the camera, in the bottom bar: a message, as no code found in an image,
        // shows above them instead of over them
        bottomBar = {
            // Scanning an image needs no camera: it stays available whatever the permission
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(insets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (multipleScan != null) {
                    MultipleScanBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        codeCount = multipleScan.codeCount,
                        isRecording = multipleScan.isRecording,
                        onFinishClicked = onMultipleScanFinished,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                ScanControls(
                    modifier = Modifier.wrapContentSize(),
                    hasTorch = permissionStatus == CameraPermissionStatus.GRANTED && cameraCapabilities?.hasTorch == true,
                    isTorchEnabled = isTorchEnabled,
                    isMultipleScanActive = multipleScan != null,
                    onScanImageClicked = onScanImageClicked,
                    onMultipleScanClicked = onMultipleScanClicked,
                    onTorchClicked = onTorchClicked,
                )
            }
        },
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
                        hint = stringResource(
                            if (multipleScan != null) Res.string.pass_codes_in_front_of_camera else Res.string.place_code_in_frame,
                        ),
                        lens = cameraLens,
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

            ScanTopActions(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(contentPadding)
                    .padding(all = 16.dp)
                    .wrapContentSize(),
                onCreateClicked = onCreateClicked,
                onHistoryClicked = onHistoryClicked,
                onSettingsClicked = onSettingsClicked,
            )
        }
    }

    when (result) {
        is ScanResult.Code -> ScanResultSheet(
            modifier = Modifier.fillMaxWidth(),
            result = result.item,
            onActionClicked = onResultActionClicked,
            onCopyClicked = { onCopyResultClicked(result.item.rawValue) },
            onSeeDetailClicked = onSeeResultDetailClicked,
            onDismissRequest = onResultDismissed,
            snackbarState = resultSnackbarState,
        )

        is ScanResult.Group -> ScanGroupResultSheet(
            modifier = Modifier.fillMaxWidth(),
            codes = result.codes,
            onCodeClicked = onResultCodeClicked,
            onSeeGroupClicked = onSeeResultGroupClicked,
            onDismissRequest = onResultDismissed,
        )

        null -> Unit
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
            cameraLens = CameraLens.BACK,
            isTorchEnabled = false,
            cameraCapabilities = CameraCapabilities(
                hasTorch = true,
                minZoomRatio = 1f,
                maxZoomRatio = 8f,
            ),
            multipleScan = null,
            result = null,
            snackbarState = rememberSCSnackbarState(),
            resultSnackbarState = rememberSCSnackbarState(),
            onBarcodeDetected = {},
            onCameraCapabilitiesChanged = {},
            onCameraUnavailable = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            onScanImageClicked = {},
            onTorchClicked = {},
            onMultipleScanClicked = {},
            onMultipleScanFinished = {},
            onCreateClicked = {},
            onHistoryClicked = {},
            onSettingsClicked = {},
            onResultActionClicked = {},
            onCopyResultClicked = {},
            onSeeResultDetailClicked = {},
            onResultCodeClicked = {},
            onSeeResultGroupClicked = {},
            onResultDismissed = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanScreen_MultipleScan() {
    SCTheme {
        ScanScreen(
            permissionStatus = CameraPermissionStatus.GRANTED,
            isCameraUnavailable = false,
            isDetectionPaused = false,
            cameraLens = CameraLens.BACK,
            isTorchEnabled = false,
            cameraCapabilities = CameraCapabilities(
                hasTorch = true,
                minZoomRatio = 1f,
                maxZoomRatio = 8f,
            ),
            multipleScan = MultipleScanSession(codeCount = 3),
            result = null,
            snackbarState = rememberSCSnackbarState(),
            resultSnackbarState = rememberSCSnackbarState(),
            onBarcodeDetected = {},
            onCameraCapabilitiesChanged = {},
            onCameraUnavailable = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            onScanImageClicked = {},
            onTorchClicked = {},
            onMultipleScanClicked = {},
            onMultipleScanFinished = {},
            onCreateClicked = {},
            onHistoryClicked = {},
            onSettingsClicked = {},
            onResultActionClicked = {},
            onCopyResultClicked = {},
            onSeeResultDetailClicked = {},
            onResultCodeClicked = {},
            onSeeResultGroupClicked = {},
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
            cameraLens = CameraLens.BACK,
            isTorchEnabled = false,
            cameraCapabilities = null,
            multipleScan = null,
            result = null,
            snackbarState = rememberSCSnackbarState(),
            resultSnackbarState = rememberSCSnackbarState(),
            onBarcodeDetected = {},
            onCameraCapabilitiesChanged = {},
            onCameraUnavailable = {},
            onAllowCameraClicked = {},
            onOpenSettingsClicked = {},
            onScanImageClicked = {},
            onTorchClicked = {},
            onMultipleScanClicked = {},
            onMultipleScanFinished = {},
            onCreateClicked = {},
            onHistoryClicked = {},
            onSettingsClicked = {},
            onResultActionClicked = {},
            onCopyResultClicked = {},
            onSeeResultDetailClicked = {},
            onResultCodeClicked = {},
            onSeeResultGroupClicked = {},
            onResultDismissed = {},
        )
    }
}
package fr.skyle.scanny.platform.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val state = remember { IosCameraPermissionState() }

    // Back from the settings of the device, where the permission may have changed
    LifecycleResumeEffect(state) {
        state.refresh()
        onPauseOrDispose {}
    }

    return state
}

private class IosCameraPermissionState : CameraPermissionState {

    override var status by mutableStateOf(currentCameraPermissionStatus())
        private set

    override fun request() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
            dispatch_async(dispatch_get_main_queue()) {
                // iOS asks only once: a refusal is for good
                status = if (isGranted) CameraPermissionStatus.GRANTED else CameraPermissionStatus.DENIED_FOREVER
            }
        }
    }

    override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url = settingsUrl,
            options = emptyMap<Any?, Any?>(),
            completionHandler = null,
        )
    }

    fun refresh() {
        status = currentCameraPermissionStatus()
    }
}

private fun currentCameraPermissionStatus(): CameraPermissionStatus =
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> CameraPermissionStatus.GRANTED
        AVAuthorizationStatusNotDetermined -> CameraPermissionStatus.UNKNOWN
        else -> CameraPermissionStatus.DENIED_FOREVER
    }